package org.grails.plugins.rx.web

import groovy.transform.CompileStatic
import rx.Observable

import java.util.concurrent.TimeUnit

/**
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class ObservableResult<T> extends TimeoutResult {

    /**
     * The observable
     */
    final Observable<T> observable


    ObservableResult(Observable<T> observable, Long timeout = null, TimeUnit unit = TimeUnit.MILLISECONDS) {
        super(timeout, unit)
        this.observable = observable
    }

}
