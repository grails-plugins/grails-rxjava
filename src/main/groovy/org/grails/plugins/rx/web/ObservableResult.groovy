package org.grails.plugins.rx.web

import groovy.transform.CompileStatic
import rx.Observable

import java.util.concurrent.TimeUnit

/**
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class ObservableResult<T> {

    /**
     * The observable
     */
    final Observable<T> observable

    /**
     * The timeout, null indicates use the default container timeout
     */
    final Long timeout

    /**
     * The time unit
     */
    final TimeUnit unit

    ObservableResult(Observable<T> observable, Long timeout = null, TimeUnit unit = TimeUnit.MILLISECONDS) {
        this.observable = observable
        this.timeout = timeout
        this.unit = unit
    }

    /**
     * The timeout, null indicates use the default container timeout
     */
    Long timeoutInMillis() {
        if(timeout != null) {
            return unit.toMillis(timeout)
        }
        return null
    }
}
