package org.grails.plugins.rx.renderer

import grails.rest.render.ContainerRenderer
import grails.rest.render.RenderContext
import grails.rest.render.RendererRegistry
import grails.util.GrailsWebUtil
import grails.web.mime.MimeType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.plugins.rx.web.RxResultTransformer
import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.beans.factory.annotation.Autowired
import rx.Observable

/**
 * A renderer for observables
 *
 * @since 1.0
 * @author Graeme Rocher
 */
@CompileStatic
class ObservableRenderer implements ContainerRenderer<Observable,Object>{
    final Class<Object> componentType = Object
    final Class<Observable> targetType = Observable
    final MimeType[] mimeTypes = [ MimeType.HTML, MimeType.JSON, MimeType.HAL_JSON, MimeType.XML, MimeType.HAL_XML ] as MimeType[]
    String encoding = GrailsWebUtil.DEFAULT_ENCODING

    @Autowired
    RxResultTransformer rxResultTransformer

    @Override
    void render(Observable object, RenderContext context) {
        final mimeType = context.acceptMimeType ?: MimeType.JSON
        context.setContentType( GrailsWebUtil.getContentType(mimeType.name, encoding) )

        ServletRenderContext servletRenderContext = (ServletRenderContext) context
        rxResultTransformer.transformActionResult(servletRenderContext.webRequest, context.viewName, object, true)
    }

    @Autowired(required = false)
    @CompileDynamic
    void setRendererRegistry(RendererRegistry registry) {
        registry.addContainerRenderer(Object, this)
    }
}
