package org.grails.plugins.rx

import grails.artefact.Controller
import grails.artefact.controller.RestResponder
import grails.converters.JSON
import grails.rx.web.helper.RxHelper
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.util.GrailsWebMockUtil
import io.reactivex.Emitter
import org.grails.plugins.rx.web.RxResultTransformer
import org.grails.plugins.rx.web.StreamingNewObservableResult
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
class EventSpec extends Specification {

    void "test stream an observable"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        EventController controller = new EventController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"An action is rendered that creates an observable"
        def observable = controller.stream()

        then:"The result is correct"
        observable instanceof StreamingNewObservableResult

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "stream", observable)
        then:"null is returned"
        result == null
        webRequest.response.contentType == RxResultTransformer.CONTENT_TYPE_EVENT_STREAM
        webRequest.response.contentAsString == ''': potato
id: 0
event: Event 0
data: Foo 0
data: Foo
data: Bar
data: Baz

: potato
id: 1
event: Event 1
data: Foo 1
data: Foo
data: Bar
data: Baz

: potato
id: 2
event: Event 2
data: Foo 2
data: Foo
data: Bar
data: Baz

: potato
id: 3
event: Event 3
data: Foo 3
data: Foo
data: Bar
data: Baz

'''

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }

    void "test stream json as an observable"() {
        setup:
        GrailsWebRequest webRequest = GrailsWebMockUtil.bindMockWebRequest()
        MockHttpServletRequest request = webRequest.getCurrentRequest()
        request.setAsyncSupported(true)
        EventController controller = new EventController()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)

        when:"An action is rendered that creates an observable"
        def observable = controller.streamJson()

        then:"The result is correct"
        observable instanceof StreamingNewObservableResult

        when:"The observable is transformed"
        RxResultTransformer transformer = new RxResultTransformer()
        def result = transformer.transformActionResult(webRequest, "stream", observable)
        then:"null is returned"
        result == null
        webRequest.response.contentType == RxResultTransformer.CONTENT_TYPE_EVENT_STREAM
        webRequest.response.contentAsString == '''id: 0
retry: 1000
data: {"foo":"bar 0\\nbar 0","baz":3}

id: 1
retry: 1000
data: {"foo":"bar 1\\nbar 1","baz":3}

id: 2
retry: 1000
data: {"foo":"bar 2\\nbar 2","baz":3}

id: 3
retry: 1000
data: {"foo":"bar 3\\nbar 3","baz":3}

'''

        cleanup:
        RequestContextHolder.setRequestAttributes(null)
    }

}

class EventController implements Controller, RestResponder{

    RxHelper rx = new RxHelper()

    def stream() {
        rx.stream { Emitter subscriber ->
            for(i in 0..3) {
                subscriber.onNext(
                        rx.event("Foo $i\nFoo\nBar\nBaz", event: "Event $i", comment: 'potato', id: "$i")
                )
            }
            subscriber.onComplete()
        }
    }

    def streamJson() {
        rx.stream { Emitter subscriber ->
            for(i in 0..3) {
                def foo =  """bar $i
bar $i"""
                subscriber.onNext(
                        rx.event([foo: foo, 'baz': 3] as JSON, id: "$i", retry: 1000)
                )
            }
            subscriber.onComplete()
        }
    }
}