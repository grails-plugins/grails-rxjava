package grails.rx.web

import grails.artefact.Controller
import grails.rx.web.helper.RxHelper
import groovy.transform.CompileStatic

/**
 * Helps creating Rx responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait RxController extends Controller {
    /**
     * Helper object for creating Rx responses
     */
    RxHelper rx = new RxHelper()
}