package org.grails.plugins.rx.web

/**
 * A result that is streaming
 *
 * @author Graeme Rocher
 * @since 1.0
 */
trait StreamingResult {
    /**
     * The event name, defaults to null
     */
    String eventName

}