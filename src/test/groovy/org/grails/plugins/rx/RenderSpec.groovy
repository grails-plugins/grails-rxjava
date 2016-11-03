package org.grails.plugins.rx

import grails.artefact.Controller
import grails.util.GrailsWebMockUtil
import io.reactivex.Observable
import org.grails.plugins.rx.web.RxResultTransformer
import org.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.ModelAndView
import spock.lang.Specification

import static grails.rx.web.Rx.*

/**
 * Created by graemerocher on 24/07/2016.
 */
class RenderSpec extends Specification {

    void "Test render a string async"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        RenderController controller = new RenderController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"A controller uses the render method and a string"
        Observable observable = controller.renderText()

        then:
        observable != null

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "renderText", observable)
        then:"null is returned"
        result == null
        webRequest.response.contentAsString == "Foo"

        cleanup:
        ConvertersConfigurationHolder.clear()
        RequestContextHolder.setRequestAttributes(null)

    }

    void "Test render a closure async"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        RenderController controller = new RenderController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"A controller uses the render method and a closure"
        Observable observable = controller.renderClosure()

        then:
        observable != null

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "renderText", observable)
        then:"null is returned"
        result == null
        webRequest.response.contentAsString == "<foo>bar</foo>"

        cleanup:
        ConvertersConfigurationHolder.clear()
        RequestContextHolder.setRequestAttributes(null)

    }

    void "Test render map with closure async"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        RenderController controller = new RenderController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"A controller uses the render method and a closure"
        Observable observable = controller.renderClosureAndMap()

        then:
        observable != null

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "renderClosureAndMap", observable)
        then:"null is returned"
        result == null
        webRequest.response.contentAsString == '{"foo":"bar"}'

        cleanup:
        ConvertersConfigurationHolder.clear()
        RequestContextHolder.setRequestAttributes(null)

    }


    void "Test render a view async"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        RenderController controller = new RenderController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"A controller uses the render method and a closure"
        Observable observable = controller.renderView()

        then:
        observable != null

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "renderView", observable)
        ModelAndView modelAndView = webRequest.getCurrentRequest().getAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW)

        then:"null is returned"
        result == null
        modelAndView != null
        modelAndView.viewName == "/render/foo"
        modelAndView.model == [foo:"Foo"]

        cleanup:
        ConvertersConfigurationHolder.clear()
        RequestContextHolder.setRequestAttributes(null)

    }
}

class RenderController implements Controller {

    def renderView() {
        Observable.just("Foo")
                .map { String result ->
            render(view:"foo", model:[foo:result])
        }
    }

    def renderText() {
        Observable.just("Foo")
        .map { String result ->
            render(result)
        }
    }

    def renderClosure() {
        Observable.just("bar")
                .map { String result ->
            render {
                foo result
            }
        }
    }

    def renderClosureAndMap() {
        Observable.just("bar")
                .map { String result ->
            render(contentType:"application/json") {
                foo result
            }
        }
    }
}
