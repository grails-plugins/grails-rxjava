package org.grails.plugins.rx.web.result

import groovy.transform.CompileStatic
import org.grails.web.converters.Converter

/**
 * Created by graemerocher on 25/07/2016.
 */
@CompileStatic
class RenderConverterResult implements RxResult<Converter>{
    RenderConverterResult(Converter converter) {
        this.result = converter
    }

    @Override
    RxCompletionStrategy execute() {
        controller.render(result)
        return RxCompletionStrategy.COMPLETE
    }
}
