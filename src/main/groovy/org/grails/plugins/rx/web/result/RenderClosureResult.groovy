package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic

/**
 * Handles the render method that accepts a closure
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RenderClosureResult implements RxResult<Closure> {
    final Map arguments

    RenderClosureResult(Closure callable, Map arguments = null) {
        this.arguments = arguments
        this.result = callable
    }

    @Override
    RxCompletionStrategy execute() {
        if(arguments != null) {
            controller.render(arguments, result)
        }
        else {
            controller.render(result)
        }
        return RxCompletionStrategy.COMPLETE
    }
}
