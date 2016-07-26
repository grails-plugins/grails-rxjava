package grails.rx.web

import grails.web.databinding.DataBindingUtils
import grails.web.mapping.mvc.exceptions.CannotRedirectException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.plugins.rx.web.result.*
import org.grails.web.converters.Converter
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.async.WebAsyncManager
import org.springframework.web.context.request.async.WebAsyncUtils
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.observables.AsyncOnSubscribe

import javax.servlet.http.HttpServletRequest

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
                Thread.start {
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
    /**
     * Executes a forward
     *
     * @param argMap The forward arguments
     */
    static RxResult<Map> forward(Map argMap) {
        new ForwardResult(argMap)
    }
    static void redirect(Map argMap) {
        throw new CannotRedirectException("Cannot redirect in an asynchronous response. Use forward insead")
    }

    static void redirect(Object arg) {
        throw new CannotRedirectException("Cannot redirect in an asynchronous response. Use forward insead")
    }
}
