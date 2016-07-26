package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic

/**
 * Implements the render method for Rx responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RenderTextResult implements RxResult<CharSequence> {

    final Map arguments

    RenderTextResult(CharSequence text, Map arguments = null) {
        this.arguments = arguments
        this.result = text
    }

    RxCompletionStrategy execute() {
        if(arguments == null) {
            controller.render(result)
        }
        else {
            controller.render(arguments, result)
        }
        return RxCompletionStrategy.COMPLETE
    }
}
