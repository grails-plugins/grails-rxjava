package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic

/**
 * Handlers the render method that takes a map
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class RenderMapResult implements RxResult<Map> {
    @Override
    RxCompletionStrategy execute() {
        controller.render(result)
        if(controller.getModelAndView() != null) {
            return RxCompletionStrategy.DISPATCH
        }
        else {
            return RxCompletionStrategy.COMPLETE
        }
    }
}
