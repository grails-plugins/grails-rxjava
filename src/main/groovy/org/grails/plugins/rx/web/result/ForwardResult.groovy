package org.grails.plugins.rx.web.result

import grails.web.UrlConverter
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic
import org.grails.web.mapping.mvc.UrlMappingsHandlerMapping
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.grails.web.util.WebUtils
import org.springframework.web.context.request.WebRequest
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.AsyncContext
import javax.servlet.RequestDispatcher

/**
 * Implements forwarding for an Async response
 *
 * @author Graeme rocher
 * @since 6.0
 */
@CompileStatic
class ForwardResult implements RxResult<Map> {
    LinkGenerator linkGenerator
    UrlConverter urlConverter

    ForwardResult(Map params) {
        this.result = params
    }

    @Override
    RxCompletionStrategy execute() {
        return RxCompletionStrategy.NONE
    }

    Map getParams() {
        this.result
    }

    void forward(GrailsWebRequest webRequest, AsyncContext asyncContext) {
        if(linkGenerator == null) throw new IllegalStateException("LinkGenerator cannot be null")
        def controllerName
        if(params.controller) {
            controllerName = params.controller
        } else {
            controllerName = webRequest.controllerName
        }

        if(controllerName) {
            def convertedControllerName = convert(controllerName.toString())
            webRequest.controllerName = convertedControllerName
        }
        params.controller = webRequest.controllerName

        if(params.action) {
            params.action = convert(params.action.toString())
        }

        if(params.namespace) {
            params.namespace = params.namespace
        }

        if(params.plugin) {
            params.plugin = params.plugin
        }

        def model = params.model instanceof Map ? params.model : Collections.EMPTY_MAP

        def request = webRequest.currentRequest

        WebUtils.exposeRequestAttributes(request, (Map)model);

        request.setAttribute(GrailsApplicationAttributes.FORWARD_IN_PROGRESS, true)
        params.includeContext = false
        String forwardURI = linkGenerator.link(params)



        def requestScope = WebRequest.SCOPE_REQUEST
        webRequest.removeAttribute(GrailsApplicationAttributes.MODEL_AND_VIEW, requestScope)
        webRequest.removeAttribute(GrailsApplicationAttributes.GRAILS_CONTROLLER_CLASS_AVAILABLE, requestScope)
        webRequest.removeAttribute(UrlMappingsHandlerMapping.MATCHED_REQUEST, requestScope)
        webRequest.removeAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, requestScope)
        webRequest.removeAttribute("grailsWebRequestFilter" + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX, requestScope)
        webRequest.setAttribute(org.springframework.web.util.WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, forwardURI, requestScope)
        asyncContext.dispatch(forwardURI)
    }

    protected String convert(String token) {
        if(urlConverter != null) {
            return urlConverter.toUrlElement(token)
        }
        return token
    }
}
