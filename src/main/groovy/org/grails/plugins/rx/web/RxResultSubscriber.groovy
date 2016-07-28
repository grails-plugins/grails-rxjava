package org.grails.plugins.rx.web

import grails.artefact.Controller
import grails.artefact.controller.RestResponder
import grails.core.GrailsControllerClass
import grails.web.UrlConverter
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugins.rx.web.result.ForwardResult
import org.grails.plugins.rx.web.result.RxCompletionStrategy
import org.grails.plugins.rx.web.result.RxResult
import org.grails.plugins.web.async.GrailsAsyncContext
import org.grails.web.errors.GrailsExceptionResolver
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.grails.web.util.WebUtils
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder
import rx.Subscriber

import javax.servlet.AsyncEvent
import javax.servlet.AsyncListener
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *  <p>A {@link Subscriber} that processed items emitted from an {@link rx.Observable} and produces
 *  an appropriate response.</p>
 *
 *  <p>If the {@link rx.Observable} emits a {@link RxResult} then processing is delegated to the result with the execute method
 *  being wrapped in the asynchronous request.</p>
 *
 *  <p>Otherwise the current controller's respond method is called with the object emitted from the observable</p>
 *
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
@Slf4j
class RxResultSubscriber extends Subscriber implements AsyncListener {
    /**
     * The Async context
     */
    final GrailsAsyncContext asyncContext
    /**
     * The exception handler
     */
    final GrailsExceptionResolver exceptionResolver

    /**
     * The controller class
     */
    final GrailsControllerClass controllerClass
    /**
     * The controller instance
     */
    final Controller controller

    /**
     * The link generator
     */
    final LinkGenerator linkGenerator

    /**
     * The url converter
     */
    UrlConverter urlConverter

    boolean isRender = false

    protected RxCompletionStrategy completionStrategy = RxCompletionStrategy.DEFAULT

    RxResultSubscriber(GrailsAsyncContext asyncContext,
                       GrailsExceptionResolver exceptionResolver,
                       LinkGenerator linkGenerator,
                       GrailsControllerClass controllerClass,
                       Controller controller) {
        this.asyncContext = asyncContext
        this.exceptionResolver = exceptionResolver
        this.controllerClass = controllerClass
        this.controller = controller
        this.linkGenerator = linkGenerator
        this.asyncContext.addListener(this)
    }

    @Override
    void onNext(Object o) {
        if(o instanceof RxResult) {
            // if the object emitted is an RxResult handle it accordingly
            if(o instanceof ForwardResult) {
                // forward results are passed the AsyncContext such that
                // the forward can call asyncContext.dispatch(..)
                ForwardResult forwardResult = (ForwardResult)o
                forwardResult.setLinkGenerator(linkGenerator)
                forwardResult.setUrlConverter(urlConverter)
                withRequest { GrailsWebRequest webRequest ->
                    forwardResult.forward(webRequest, asyncContext)
                }
                completionStrategy = RxCompletionStrategy.NONE
            }
            else {
                // Regular RxResults are wrapped in the asynchronous
                // request and executed. The completion strategy dictates
                // what happens next
                RxResult result = (RxResult) o
                result.controller = controller
                withRequest {
                    completionStrategy = result.execute()
                }
            }
        }
        else if(o instanceof HttpStatus) {
            // if the item emitted is an HttpStatus object set the status
            // of the response and terminate proccessing
            withRequest {
                controller.render(status: o)
                completionStrategy = RxCompletionStrategy.COMPLETE
            }
        }
        else {
            // Otherwise delegate to the controller respond method
            withRequest { GrailsWebRequest webRequest ->
                ((RestResponder)controller).respond((Object)o)
                def modelAndView = webRequest.getRequest().getAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW)
                if(modelAndView != null) {
                    completionStrategy = RxCompletionStrategy.DISPATCH
                }
                else {
                    completionStrategy = RxCompletionStrategy.DEFAULT
                }
            }
        }
    }

    /**
     * Wrap the given closure in asynchronous request processing
     *
     * @param callable The closure
     * @return The return result of the closure
     */
    public <T> T withRequest(Closure<T> callable) {
        def previousAttributes = RequestContextHolder.getRequestAttributes()
        GrailsWebRequest rxWebRequest = new GrailsWebRequest((HttpServletRequest)asyncContext.request, (HttpServletResponse)asyncContext.response, asyncContext.request.getServletContext())
        WebUtils.storeGrailsWebRequest(rxWebRequest)

        try {
            callable.call(rxWebRequest)
        } finally {
            rxWebRequest.requestCompleted()
            WebUtils.clearGrailsWebRequest()
            if(previousAttributes != null) {
                RequestContextHolder.setRequestAttributes(previousAttributes)
            }
        }
    }

    @Override
    void onCompleted() {
        // When the observable finishes emitting items
        // terminate the asynchronous context in the appropriate manner based on the
        // completion strategy
        switch(completionStrategy) {
            case RxCompletionStrategy.COMPLETE:
                asyncContext.complete()
                break
            case RxCompletionStrategy.DISPATCH:
                asyncContext.dispatch()
                break
            case RxCompletionStrategy.NONE:
                // for none, the RxResult will have terminated the asynchronous context so do nothing
                break
            default:
                if(isRender) {
                    asyncContext.response.flushBuffer()
                }
                asyncContext.complete()
        }
    }

    @Override
    void onError(Throwable e) {
        if(!asyncContext.response.isCommitted()) {
            // if an error occurred and the response has not yet been commited try and handle it
            def httpServletResponse = (HttpServletResponse) asyncContext.response
            // first check if the exception resolver and resolve a model and view
            if(exceptionResolver != null) {
                def modelAndView = exceptionResolver.resolveException((HttpServletRequest) asyncContext.request, httpServletResponse, this, (Exception) e)
                if(modelAndView != null) {
                    asyncContext.getRequest().setAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW, modelAndView);
                    asyncContext.dispatch()
                }
                else {
                    // if the error can't be resolved send the default error
                    sendDefaultError(e, httpServletResponse)
                }
            }
            else {
                sendDefaultError(e, httpServletResponse)
            }
        }
        else {
            log.error("Async Dispatch Error: ${e.message}", e)
            asyncContext.complete()
        }
    }

    @Override
    void onComplete(AsyncEvent event) throws IOException {
        if(!isUnsubscribed()) {
            unsubscribe()
        }
    }

    @Override
    void onTimeout(AsyncEvent event) throws IOException {
        if(!isUnsubscribed()) {
            unsubscribe()
            onError(event.throwable)
        }
    }

    @Override
    void onError(AsyncEvent event) throws IOException {
        if(!isUnsubscribed()) {
            unsubscribe()
            onError(event.throwable)
        }
    }

    @Override
    void onStartAsync(AsyncEvent event) throws IOException {
        // no-op
    }

    protected void sendDefaultError(Throwable e, HttpServletResponse httpServletResponse) {
        log.error("Async Dispatch Error: ${e.message}", e)
        httpServletResponse.sendError(500, "Async Dispatch Error: ${e.message}")
        asyncContext.complete()
    }
}
