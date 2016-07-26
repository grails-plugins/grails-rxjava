package org.grails.plugins.rx

import grails.artefact.Controller
import grails.artefact.controller.RestResponder
import grails.converters.JSON
import grails.core.DefaultGrailsApplication
import grails.persistence.Entity
import grails.util.GrailsWebMockUtil
import org.grails.plugins.rx.web.RxResultTransformer
import org.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.grails.web.converters.marshaller.json.DomainClassMarshaller
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.ModelAndView
import rx.Observable
import spock.lang.Specification
import static grails.rx.web.Rx.*
/**
 * Created by graemerocher on 24/07/2016.
 */
class RespondSpec extends Specification {

    void "Test render a string async"() {

        setup:
        def application = new DefaultGrailsApplication(Book)
        application.initialise()
        JSON.registerObjectMarshaller(new DomainClassMarshaller(false, application))
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        RespondController controller = new RespondController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)
        request.addHeader("Accept", "application/json")
        when:"A controller uses the render method and a string"
        Observable observable = controller.respondWithEntity()

        then:
        observable != null

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "renderText", observable)
        ModelAndView modelAndView = webRequest.getCurrentRequest().getAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW)

        then:"null is returned"
        result == null
        modelAndView == null
        webRequest.response.contentAsString == '{"title":"The Stand"}'

        cleanup:
        ConvertersConfigurationHolder.clear()
        RequestContextHolder.setRequestAttributes(null)
    }

}

class RespondController implements Controller, RestResponder {

    def respondWithEntity() {
        Observable.just(new Book(title: "The Stand"))
                  .map { Book b ->
            respond b
        }
    }
}

@Entity
class Book {
    String title
}
