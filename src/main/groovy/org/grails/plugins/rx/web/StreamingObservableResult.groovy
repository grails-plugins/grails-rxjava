package org.grails.plugins.rx.web

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Indicates a Comet streaming response should be created
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
@InheritConstructors
class StreamingObservableResult<T> extends ObservableResult<T> {
    /**
     * The event name, defaults to null
     */
    String eventName
}
