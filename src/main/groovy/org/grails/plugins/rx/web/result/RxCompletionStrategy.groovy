package org.grails.plugins.rx.web.result

/**
 * @author Graeme Rocher
 * @since 1.0
 */
enum RxCompletionStrategy {
    /**
     * Dispatch the underlying container
     */
    DISPATCH,
    /**
     * Complete the async request
     */
    COMPLETE,
    /**
     * Apply the default handling
     */
    DEFAULT,
    /**
     * Don't do anything
     */
    NONE
}