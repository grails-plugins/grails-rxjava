package org.grails.plugins.rx.web.result

import grails.artefact.controller.RestResponder
import groovy.transform.CompileStatic

/**
 * RxResult for ther respond method
 *
 * @since 1.0
 * @author Graeme Rocher
 */
@CompileStatic
class RespondResult implements RxResult<Object> {

    final Map arguments

    RespondResult(Object object, Map arguments = null) {
        this.arguments = arguments
        this.result = object
    }

    @Override
    RxCompletionStrategy execute() {
        RestResponder restResponder = (RestResponder) controller
        if(arguments != null) {
            restResponder.respond(result, arguments)
        }
        else {
            restResponder.respond(result)
        }
        if(controller.getModelAndView() != null) {
            return RxCompletionStrategy.DISPATCH
        }
        else {
            return RxCompletionStrategy.COMPLETE
        }
    }
}
