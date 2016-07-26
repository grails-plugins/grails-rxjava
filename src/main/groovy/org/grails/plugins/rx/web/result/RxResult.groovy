package org.grails.plugins.rx.web.result

import grails.artefact.Controller

/**
 * @author Graeme Rocher
 * @since 1.0
 */
trait RxResult<T> {

    /**
     * The controller
     */
    Controller controller

    /**
     * The result of the observable
     */
    T result

    /**
     * Execute the given result for the result of an observable
     *
     * @param result
     */
    abstract RxCompletionStrategy execute()
}