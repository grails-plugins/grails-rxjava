package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RenderMapWritableResult implements RxResult<Writable> {

    final Map arguments

    RenderMapWritableResult(Writable writable, Map arguments) {
        this.arguments = arguments
        this.result = writable
    }

    @Override
    RxCompletionStrategy execute() {
        controller.render(arguments, result)
        return RxCompletionStrategy.COMPLETE
    }
}
