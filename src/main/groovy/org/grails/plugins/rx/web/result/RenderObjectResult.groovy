package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic

/**
 * Implements the render method for Rx responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RenderObjectResult implements RxResult<Object> {
    @Override
    RxCompletionStrategy execute() {
        controller.render(result)
        return RxCompletionStrategy.COMPLETE
    }
}
