package org.grails.plugins.rx.web

import grails.artefact.Controller
import grails.async.web.AsyncGrailsWebRequest
import grails.web.UrlConverter
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import org.grails.plugins.web.async.GrailsAsyncContext
import org.grails.web.errors.GrailsExceptionResolver
import org.grails.web.servlet.mvc.ActionResultTransformer
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.async.AsyncWebRequest
import org.springframework.web.context.request.async.WebAsyncManager
import org.springframework.web.context.request.async.WebAsyncUtils

import javax.servlet.AsyncContext
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * An {@link ActionResultTransformer} that transforms return values of type {@link Observable}.
 *
 * An asynchronous request is created and the observable subscribed to, allowing non-blocking processing of requests.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@Slf4j
class RxResultTransformer implements ActionResultTransformer {

    /**
     * For handling exceptions
     */
    public static final String CONTENT_TYPE_EVENT_STREAM = "text/event-stream"
    @Autowired(required = false)
    GrailsExceptionResolver exceptionResolver

    /**
     * The link generator is required for handling forwarding
     */
    @Autowired
    LinkGenerator linkGenerator

    @Autowired(required = false)
    UrlConverter urlConverter

    Object transformActionResult(GrailsWebRequest webRequest, String viewName, Object actionResult, boolean isRender = false) {
        TimeoutResult timeoutResult = null
        if(actionResult instanceof TimeoutResult) {
            timeoutResult = ((TimeoutResult)actionResult)
            if(timeoutResult instanceof ObservableResult) {
                actionResult = ((ObservableResult)timeoutResult).observable
            }
        }


        boolean isObservable = actionResult instanceof Observable
        if(isObservable || (actionResult instanceof NewObservableResult)) {
            // tell Grails not to render the view by convention
            HttpServletRequest request = webRequest.getCurrentRequest()
            HttpServletResponse response = webRequest.getCurrentResponse()


            webRequest.setRenderView(false)

            // Create the Async web request and register it with the WebAsyncManager so Spring is aware
            WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request)

            AsyncWebRequest asyncWebRequest = new AsyncGrailsWebRequest(
                    request,
                    response,
                    webRequest.servletContext)

            asyncManager.setAsyncWebRequest(asyncWebRequest)

            // Start async processing and create the GrailsAsync object
            asyncWebRequest.startAsync()
            request.setAttribute(GrailsApplicationAttributes.ASYNC_STARTED, true)
            GrailsAsyncContext asyncContext = new GrailsAsyncContext(asyncWebRequest.asyncContext, webRequest)
            final boolean isStreaming = timeoutResult instanceof StreamingResult
            if(timeoutResult != null) {
                def timeout = timeoutResult.timeoutInMillis()
                if(timeout != null) {
                    asyncContext.setTimeout(timeout)
                }
                if(isStreaming) {
                    response.setContentType(CONTENT_TYPE_EVENT_STREAM);
                    response.flushBuffer()
                }
            }

            RxResultSubscriber subscriber = new RxResultSubscriber(
                    asyncContext,
                    exceptionResolver,
                    linkGenerator,
                    webRequest.controllerClass,
                    (Controller)webRequest.attributes.getController(request)
            )
            subscriber.isRender = isRender
            subscriber.urlConverter = urlConverter
            if(isStreaming) {
                subscriber.serverSendEvents = true
                subscriber.serverSendEventName = ((StreamingResult)timeoutResult).eventName
            }

            // handle RxJava Observables
            if(isObservable) {

                Observable observable = (Observable)actionResult

                // in a separate thread register the observable subscriber
                asyncContext.start {
                    observable.subscribe(subscriber)
                }
            }
            else {
                NewObservableResult newObservableResult = (NewObservableResult)actionResult
                Observable newObservable = Observable.create( { ObservableEmitter newSub ->
                    asyncContext.start {
                        Closure callable = newObservableResult.callable
                        callable.setDelegate(newSub)
                        callable.call(newSub)
                    }
                } as ObservableOnSubscribe)
                newObservable.subscribe(subscriber)
            }
            // return null indicating that the request thread should be returned to the thread pool
            // async request processing will take over
            return null
        }
        return actionResult
    }
}
