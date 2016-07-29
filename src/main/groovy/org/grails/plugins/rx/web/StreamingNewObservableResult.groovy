package org.grails.plugins.rx.web

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * A new observable that is streaming
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@InheritConstructors
class StreamingNewObservableResult<T> extends NewObservableResult<T>  implements StreamingResult {
}
