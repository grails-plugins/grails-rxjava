package org.grails.plugins.rx.web

import groovy.transform.CompileStatic
import rx.Observer

import java.util.concurrent.TimeUnit

/**
 * Creates a new observable from the given closure
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class NewObservableResult<T> extends TimeoutResult {

    final Closure<T> callable

    NewObservableResult(Closure<T> callable, Long timeout = null, TimeUnit unit = TimeUnit.MILLISECONDS) {
        super(timeout, unit)
        this.callable = callable
        def parameterTypes = this.callable.parameterTypes
        boolean isSubscriber = parameterTypes.length == 1 && Observer.isAssignableFrom(parameterTypes[0])
        if(!isSubscriber) {
            throw new IllegalArgumentException("Passed closure must accept argument of type rx.Observer")
        }
    }

}
