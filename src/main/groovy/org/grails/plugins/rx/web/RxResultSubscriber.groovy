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
 * Handles results from Observables
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

    protected List rxResults = []
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
            if(o instanceof ForwardResult) {
                ForwardResult forwardResult = (ForwardResult)o
                forwardResult.setLinkGenerator(linkGenerator)
                forwardResult.setUrlConverter(urlConverter)
                withRequest { GrailsWebRequest webRequest ->
                    forwardResult.forward(webRequest, asyncContext)
                }
                completionStrategy = RxCompletionStrategy.NONE
            }
            else {
                RxResult result = (RxResult) o
                result.controller = controller
                withRequest {
                    completionStrategy = result.execute()
                }
            }
        }
        else if(o instanceof HttpStatus) {
            withRequest {
                controller.render(status: o)
                completionStrategy = RxCompletionStrategy.COMPLETE
            }
        }
        else {
            rxResults.add(o)
        }
    }

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
        switch(completionStrategy) {
            case RxCompletionStrategy.COMPLETE:
                asyncContext.complete()
                return
            case RxCompletionStrategy.DISPATCH:
                asyncContext.dispatch()
                return
            case RxCompletionStrategy.NONE:
                return
        }

        GrailsWebRequest rxWebRequest = new GrailsWebRequest((HttpServletRequest)asyncContext.request, (HttpServletResponse)asyncContext.response, asyncContext.request.getServletContext())
        WebUtils.storeGrailsWebRequest(rxWebRequest)

        try {
            if(rxResults.isEmpty()) {
                if(controller instanceof RestResponder) {
                    ((RestResponder)controller).respond((Object)null)
                }
            }
            else {
                if(rxResults.size() == 1) {
                    def first = rxResults.get(0)
                    ((RestResponder)controller).respond(first)
                }
                else {
                    ((RestResponder)controller).respond(rxResults)
                }
            }
            def modelAndView = rxWebRequest.getRequest().getAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW)
            if(modelAndView != null && !isRender) {
                asyncContext.dispatch()
            }
            else {
                if(isRender) {
                    asyncContext.response.flushBuffer()
                }
                asyncContext.complete()
            }
        } finally {
            rxWebRequest.requestCompleted()
            WebUtils.clearGrailsWebRequest()
        }
    }

    @Override
    void onError(Throwable e) {
        if(!asyncContext.response.isCommitted()) {
            def httpServletResponse = (HttpServletResponse) asyncContext.response
            if(exceptionResolver != null) {
                def modelAndView = exceptionResolver.resolveException((HttpServletRequest) asyncContext.request, httpServletResponse, this, (Exception) e)
                asyncContext.getRequest().setAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW, modelAndView);
                asyncContext.dispatch()
            }
            else {
                log.error("Async Dispatch Error: ${e.message}", e)
                httpServletResponse.sendError(500, "Async Dispatch Error: ${e.message}")
                asyncContext.complete()
            }
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
        }
    }

    @Override
    void onError(AsyncEvent event) throws IOException {
        if(!isUnsubscribed()) {
            unsubscribe()
        }
    }

    @Override
    void onStartAsync(AsyncEvent event) throws IOException {
        // no-op
    }
}
