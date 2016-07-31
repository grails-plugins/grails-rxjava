package grails.rx.web

import grails.async.Promises
import grails.web.databinding.DataBindingUtils
import grails.web.mapping.mvc.exceptions.CannotRedirectException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.plugins.rx.web.NewObservableResult
import org.grails.plugins.rx.web.ObservableResult
import org.grails.plugins.rx.web.StreamingNewObservableResult
import org.grails.plugins.rx.web.StreamingObservableResult
import org.grails.plugins.rx.web.result.*
import org.grails.web.converters.Converter
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.async.WebAsyncManager
import org.springframework.web.context.request.async.WebAsyncUtils
import rx.Observable
import rx.Subscriber

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import java.util.concurrent.TimeUnit

/**
 * Methods usable with RxJava observable transformations in order to control
 * the final controller response
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class Rx {
    /**
     * Render the given object using asynchronous request processing
     *
     * @param object The text to render
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.lang.Object)
     */
    static RxResult<Object> render(Object object) {
        new RenderObjectResult(result: object)
    }

    /**
     * Render the given text using asynchronous request processing
     *
     * @param text The text to render
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.lang.CharSequence)
     */
    static RxResult<CharSequence> render(CharSequence text) {
        new RenderTextResult(text)
    }


    /**
     * Render the given text using asynchronous request processing
     *
     * @param arguments The arguments
     * @param text The text to render
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.lang.CharSequence)
     */
    static RxResult<CharSequence> render(Map arguments, CharSequence text) {
        new RenderTextResult(text, arguments)
    }

    /**
     * Render the given closure using asynchronous request processing
     *
     * @param callable The closure
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(groovy.lang.Closure)
     */
    static RxResult<Closure> render(Closure callable) {
        new RenderClosureResult(callable)
    }

    /**
     * Execute render for the given named arguments
     *
     * @param arguments The named arguments
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.util.Map)
     */
    static RxResult<Map> render(Map arguments) {
        new RenderMapResult(result: arguments)
    }

    /**
     * Execute render for the given named arguments and closure
     *
     * @param arguments The named arguments
     * @param callable The closure
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.util.Map, groovy.lang.Closure)
     */
    static RxResult<Closure> render(Map arguments, Closure callable) {
        return new RenderClosureResult(callable, arguments)
    }

    /**
     * Executes render for the given arguments and writable
     *
     * @param arguments The arguments
     * @param writable The writable
     * @return The rx result
     */
    static RxResult<Writable> render(Map arguments, Writable writable) {
        return new RenderMapWritableResult(writable, arguments)
    }

    /**
     * Executes render for the given converter
     *
     * @param converter The converter
     * @return the rx result
     */
    static RxResult<Converter> render(Converter converter) {
        return new RenderConverterResult(converter)
    }

    /**
     */
    static RxResult<Object> respond(Map args, value) {
        respond((Object)value, args)
    }

    /**
     * The respond method will attempt to delivery an appropriate response for the
     * requested response format and Map value.
     *
     * If the value is null then a 404 will be returned. Otherwise the {@link grails.rest.render.RendererRegistry}
     * will be consulted for an appropriate response renderer for the requested response format.
     *
     * @param value The value
     * @return
     */
    static RxResult<Object> respond(Map value) {
        respond((Object)value)
    }

    /**
     * Same as {@link #respond(java.util.Map)}, but here to support Groovy named arguments
     */
    static RxResult<Object> respond(Map namedArgs, Map value) {
        respond((Object)value, namedArgs)
    }

    /**
     * The respond method will attempt to delivery an appropriate response for the
     * requested response format and value.
     *
     * If the value is null then a 404 will be returned. Otherwise the {@link grails.rest.render.RendererRegistry}
     * will be consulted for an appropriate response renderer for the requested response format.
     *
     * @param value The value
     * @param args The arguments
     * @return
     */
    static RxResult<Object> respond(value, Map args = [:]) {
        new RespondResult(value, args)
    }

    /**
     * Implements the bind data method for Rx responses
     *
     * @param object The object to bind to
     * @param bindingSource The binding source
     * @return An observable
     */
    @CompileDynamic
    static Observable bindData(Object object, Object bindingSource, Map arguments = Collections.emptyMap(), String filter = null) {
        Observable.create( { Subscriber<? super Object> subscriber ->
                subscriber.onStart()
                Promises.task {
                    if(bindingSource instanceof HttpServletRequest) {
                        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(bindingSource)
                        // horrible hack, find better solution
                        GrailsWebRequest webRequest = asyncManager != null ? asyncManager.@asyncWebRequest : null
                        if(webRequest != null) {
                            RequestContextHolder.setRequestAttributes(webRequest)
                            try {
                                List includeList = convertToListIfCharSequence(arguments?.include)
                                List excludeList = convertToListIfCharSequence(arguments?.exclude)

                                DataBindingUtils.bindObjectToInstance(object, bindingSource, includeList, excludeList, filter)
                            } finally {
                                RequestContextHolder.setRequestAttributes(null)
                            }
                        }
                        else {
                            object.properties = bindingSource
                        }
                    }
                    else {
                        object.properties = bindingSource
                    }
                    subscriber.onNext(object)
                    subscriber.onCompleted()

                }
        } as Observable.OnSubscribe<Object>)
    }


    /**
     * Creates an observable from the request body
     *
     * @param The request
     *
     * @return An observable
     */
    static Observable<InputStream> fromBody(HttpServletRequest request) {
        Observable.create( { Subscriber<InputStream> subscriber ->
            subscriber.onStart()
            Promises.task {
                InputStream inputStream = null
                try {
                    try {
                        inputStream = request.getInputStream()
                        subscriber.onNext(inputStream)
                    } catch (Throwable e) {
                        subscriber.onError(e)
                    }
                } finally {
                    subscriber.onCompleted()
                    inputStream?.close()
                }

            }
        } as Observable.OnSubscribe<InputStream>)
    }

    /**
     * Return an observable with the given timeout to be used with the container. In the event the timeout is reached
     * the containers onTimeout event handler will be invoked and an error response returned
     *
     * @param observable The observable
     * @param timeout The timeout
     * @param unit The timeout unit
     * @return An observable result
     */
    static <T> ObservableResult<T> withTimeout(Observable<T> observable, Long timeout , TimeUnit unit = TimeUnit.MILLISECONDS) {
        return new ObservableResult<T>(observable, timeout, unit)
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    static <T> NewObservableResult<T> create(@DelegatesTo(Subscriber) Closure<T> callable) {
        return new NewObservableResult<T>(callable, -1L)
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    static <T> NewObservableResult<T> create(Long timeout, TimeUnit unit, @DelegatesTo(Subscriber) Closure<T> callable) {
        return new NewObservableResult<T>(callable, timeout, unit)
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    static <T> NewObservableResult<T> create(Long timeout, @DelegatesTo(Subscriber) Closure<T> callable) {
        return new NewObservableResult<T>(callable, timeout)
    }

    /**
     * Start a streaming Server-Send event response for the given observable
     *
     * @param observable The observable
     * @param timeout The timeout
     * @param unit The timeout unit
     * @return An observable result
     */
    static <T> StreamingObservableResult<T> stream(Observable<T> observable, Long timeout = -1, TimeUnit unit = TimeUnit.MILLISECONDS) {
        return new StreamingObservableResult<T>(observable, timeout, unit)
    }

    /**
     * Start a streaming Server-Send event response for the given observable
     *
     * @param eventName The event name
     * @param observable The observable
     * @param timeout The timeout
     * @param unit The timeout unit
     * @return An observable result
     */
    static <T> StreamingObservableResult<T> stream(String eventName, Observable<T> observable, Long timeout = -1, TimeUnit unit = TimeUnit.MILLISECONDS) {
        def streamingObservableResult = new StreamingObservableResult<T>(observable, timeout, unit)
        streamingObservableResult.eventName = eventName
        return streamingObservableResult
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param timeout The timeout
     * @param unit The timeout unit
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    static <T> StreamingNewObservableResult<T> stream(Long timeout, TimeUnit unit, @DelegatesTo(Subscriber) Closure callable) {
        return new StreamingNewObservableResult<T>(callable, timeout, unit)
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param timeout The timeout in milliseconds
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    static <T> StreamingNewObservableResult<T> stream(Long timeout, @DelegatesTo(Subscriber) Closure callable) {
        return new StreamingNewObservableResult<T>(callable, timeout)
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    static <T> StreamingNewObservableResult<T> stream(@DelegatesTo(Subscriber) Closure callable) {
        return new StreamingNewObservableResult<T>(callable, -1L)
    }
    /**
     * Executes a forward
     *
     * @param argMap The forward arguments
     */
    static RxResult<Map> forward(Map argMap) {
        new ForwardResult(argMap)
    }

    /**
     * @throws CannotRedirectException Redirects are not possible in asynchronous requests
     */
    static void redirect(Map argMap) {
        throw new CannotRedirectException("Cannot redirect in an asynchronous response. Use forward insead")
    }

    /**
     * @throws CannotRedirectException Redirects are not possible in asynchronous requests
     */
    static void redirect(Object arg) {
        throw new CannotRedirectException("Cannot redirect in an asynchronous response. Use forward insead")
    }

    private static List convertToListIfCharSequence(value) {
        List result = null
        if(value instanceof CharSequence) {
            result = []
            result << (value instanceof String ? value : value.toString())
        } else if(value instanceof List) {
            result = (List)value
        }
        return result
    }
}
