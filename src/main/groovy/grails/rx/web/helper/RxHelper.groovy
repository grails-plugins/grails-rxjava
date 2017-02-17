package grails.rx.web.helper

import grails.rx.web.Rx
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import io.reactivex.Emitter
import io.reactivex.Observable
import org.codehaus.groovy.runtime.GStringImpl
import org.grails.plugins.rx.web.NewObservableResult
import org.grails.plugins.rx.web.ObservableResult
import org.grails.plugins.rx.web.StreamingNewObservableResult
import org.grails.plugins.rx.web.StreamingObservableResult
import org.grails.plugins.rx.web.result.RxResult
import org.grails.plugins.rx.web.sse.SseResult
import org.grails.web.converters.Converter

import javax.servlet.http.HttpServletRequest
import java.util.concurrent.TimeUnit

/**
 * Helper methods for creating Rx responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RxHelper {
    /**
     * Render the given object using asynchronous request processing
     *
     * @param object The text to render
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.lang.Object)
     */
    RxResult<Object> render(Object object) {
        Rx.render(object)
    }

    /**
     * Render the given text using asynchronous request processing
     *
     * @param text The text to render
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.lang.CharSequence)
     */
    RxResult<CharSequence> render(CharSequence text) {
        Rx.render(text)
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
    RxResult<CharSequence> render(Map arguments, CharSequence text) {
        Rx.render(arguments, text)
    }

    /**
     * Render the given closure using asynchronous request processing
     *
     * @param callable The closure
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(groovy.lang.Closure)
     */
    RxResult<Closure> render(Closure callable) {
        Rx.render(callable)
    }

    /**
     * Execute render for the given named arguments
     *
     * @param arguments The named arguments
     * @return The rx result
     *
     * @see grails.artefact.Controller#render(java.util.Map)
     */
    RxResult<Map> render(Map arguments) {
        Rx.render(arguments)
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
    RxResult<Closure> render(Map arguments, Closure callable) {
        Rx.render(arguments, callable)
    }

    /**
     * Executes render for the given arguments and writable
     *
     * @param arguments The arguments
     * @param writable The writable
     * @return The rx result
     */
    RxResult<Writable> render(Map arguments, Writable writable) {
        Rx.render(arguments, writable)
    }

    /**
     * Executes render for the given converter
     *
     * @param converter The converter
     * @return the rx result
     */
    RxResult<Converter> render(Converter converter) {
        Rx.render(converter)
    }

    /**
     * Create a Server Sent Event without data
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @return The SSE Event
     */
    SseResult event(Map sseOptions) {
        Rx.event(sseOptions, (Writable) null)
    }

    /**
     * Use a writable to create a Server Sent Event
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param writable The writable
     * @return The SSE Event
     */
    SseResult event(Map sseOptions, Writable writable) {
        Rx.event(sseOptions, writable)
    }

    /**
     * Use a writable to create a Server Sent Event
     *
     * @param writable The writable
     * @return The SSE Event
     */
    SseResult event(Writable writable) {
        Rx.event([:], writable)
    }

    /**
     * Use a converter to create a Server Sent Event
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param converter The converter
     * @return The SSE Event
     */
    SseResult event(Map sseOptions, Converter converter) {
        Rx.event(sseOptions, converter)
    }

    /**
     * Use a converter to create a Server Sent Event
     *
     * @param converter The converter
     * @return The SSE Event
     */
    SseResult event(Converter writable) {
        Rx.event([:], writable)
    }

    /**
     * Use a GString to create a Server Sent Event
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param gString The GString
     * @return The SSE Event
     */
    // required for GStrings in @CompileStatic, @TypeChecked otherwise compiler can't decide between Writable or CharSequence method
    SseResult event(Map sseOptions, GString gString) {
        Rx.event(sseOptions, (Writable)gString)
    }

    /**
     * Use a GString to create a Server Sent Event
     *
     * @param gString The GString
     * @return The SSE Event
     */
    // required for GStrings in @CompileStatic, @TypeChecked otherwise compiler can't decide between Writable or CharSequence method
    SseResult event(GString gString) {
        Rx.event([:], (Writable)gString)
    }

    /**
     * Use a GString to create a Server Sent Event
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param gString The GString
     * @return The SSE Event
     */
    // required for GStrings in @CompileDynamic, @TypeChecked otherwise runtime can't decide between Writable or CharSequence method
    SseResult event(Map sseOptions, GStringImpl gString) {
        Rx.event(sseOptions, (Writable)gString)
    }

    /**
     * Use a GString to create a Server Sent Event
     *
     * @param gString The GString
     * @return The SSE Event
     */
    // required for GStrings in @CompileDynamic, @TypeChecked otherwise runtime can't decide between Writable or CharSequence method
    SseResult event(GStringImpl gString) {
        Rx.event([:], (Writable)gString)
    }

    /**
     * Use a CharSequence to create a Server Sent Event
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param charSequence The CharSequence
     * @return The SSE Event
     */
    SseResult event(Map sseOptions, CharSequence charSequence) {
        Rx.event(sseOptions, charSequence)
    }

    /**
     * Use a CharSequence to create a Server Sent Event
     *
     * @param charSequence The CharSequence
     * @return The SSE Event
     */
    SseResult event(CharSequence charSequence) {
        Rx.event([:], charSequence)
    }

    /**
     * Use a Closure to create a Server Sent Event.  The closure will be passed a {@link Writer} as the only argument
     * which it can use to write the data for the event to.
     *
     * @param sseOptions optional Server Sent Event arguments (comment, id, event, retry)
     * @param closure The closure
     * @return The SSE Event
     */
    SseResult event(Map sseOptions, @ClosureParams(value = SimpleType, options = ['java.io.Writer']) Closure closure) {
        Rx.event(sseOptions, closure)
    }

    /**
     * Use a Closure to create a Server Sent Event.  The closure will be passed a {@link Writer} as the only argument
     * which it can use to write the data for the event to.
     *
     * @param closure The closure
     * @return The SSE Event
     */
    SseResult event(@ClosureParams(value = SimpleType, options = ['java.io.Writer']) Closure closure) {
        Rx.event([:], closure)
    }

    /**
     * The respond method will attempt to delivery an appropriate response for the
     * requested response format and Map value.
     *
     * If the value is null then a 404 will be returned. Otherwise the {@link grails.rest.render.RendererRegistry}
     * will be consulted for an appropriate response renderer for the requested response format.
     *
     * @param args the arguments
     * @param value The value
     * @return The rx result
     */
    RxResult<Object> respond(Map args, value) {
        Rx.respond(args,value)
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
    RxResult<Object> respond(Map value) {
        Rx.respond(value)
    }

    /**
     * Same as {@link #respond(java.util.Map)}, but here to support Groovy named arguments
     */
    RxResult<Object> respond(Map namedArgs, Map value) {
        Rx.respond(namedArgs, value)
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
    RxResult<Object> respond(value, Map args = [:]) {
        Rx.respond(value, args)
    }

    /**
     * Implements the bind data method for Rx responses
     *
     * @param object The object to bind to
     * @param bindingSource The binding source
     * @return An observable
     */
    @CompileDynamic
    Observable bindData(Object object, Object bindingSource, Map arguments = Collections.emptyMap(), String filter = null) {
        Rx.bindData(object, bindingSource, arguments, filter)
    }


    /**
     * Creates an observable from the request body
     *
     * @param The request
     *
     * @return An observable
     */
    Observable<InputStream> fromBody(HttpServletRequest request) {
        Rx.fromBody(request)
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
    public <T> ObservableResult<T> withTimeout(Observable<T> observable, Long timeout, TimeUnit unit = TimeUnit.MILLISECONDS) {
        Rx.withTimeout(observable, timeout, unit)
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    public <T> NewObservableResult<T> create(@DelegatesTo(Emitter) Closure<T> callable) {
        Rx.create callable
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    public <T> NewObservableResult<T> create(Long timeout, TimeUnit unit, @DelegatesTo(Emitter) Closure<T> callable) {
        Rx.create timeout, unit, callable
    }

    /**
     * Create a new observable result for the given closure. The closure should accept an argument of type rx.Subscriber
     * @param callable The closure
     * @return The observable result
     */
    public <T> NewObservableResult<T> create(Long timeout, @DelegatesTo(Emitter) Closure<T> callable) {
        Rx.create timeout, callable
    }

    /**
     * Start a streaming Server-Send event response for the given observable
     *
     * @param observable The observable
     * @param timeout The timeout
     * @param unit The timeout unit
     * @return An observable result
     */
    public <T> StreamingObservableResult<T> stream(Observable<T> observable, Long timeout = -1, TimeUnit unit = TimeUnit.MILLISECONDS) {
        Rx.stream observable, timeout, unit
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
    public <T> StreamingObservableResult<T> stream(String eventName, Observable<T> observable, Long timeout = -1, TimeUnit unit = TimeUnit.MILLISECONDS) {
        Rx.stream eventName, observable, timeout, unit
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param timeout The timeout
     * @param unit The timeout unit
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    public <T> StreamingNewObservableResult<T> stream(Long timeout, TimeUnit unit, @DelegatesTo(Emitter) Closure callable) {
        Rx.stream timeout, unit, callable
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param timeout The timeout in milliseconds
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    public <T> StreamingNewObservableResult<T> stream(Long timeout, @DelegatesTo(Emitter) Closure callable) {
        Rx.stream timeout, callable
    }

    /**
     * Start a streaming Server-Send event response for the given closure which is converted to an asynchronous task
     *
     * @param callable The closure, it should accept a single argument which is the rx.Subscriber instance
     * @return An observable result
     */
    public <T> StreamingNewObservableResult<T> stream(@DelegatesTo(Emitter) Closure callable) {
        Rx.stream callable
    }
    /**
     * Executes a forward
     *
     * @param argMap The forward arguments
     */
    RxResult<Map> forward(Map argMap) {
        Rx.forward(argMap)
    }

}
