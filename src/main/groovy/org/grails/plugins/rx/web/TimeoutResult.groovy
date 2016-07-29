package org.grails.plugins.rx.web

import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

/**
 * A result that provides a timeout
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
abstract class TimeoutResult {
    /**
     * The timeout, null indicates use the default container timeout
     */
    final Long timeout

    /**
     * The time unit
     */
    final TimeUnit unit

    TimeoutResult(Long timeout, TimeUnit unit) {
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
