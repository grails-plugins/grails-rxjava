package org.grails.plugins.rx

import grails.plugins.*
import org.grails.plugins.rx.renderer.ObservableRenderer
import org.grails.plugins.rx.web.RxResultTransformer

class RxjavaGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "RxJava for Grails" // Headline display name of the plugin
    def author = "Graeme Rocher"
    def authorEmail = "graeme@grails.org"
    def description = '''\
A plugin that integrates RxJava with Grails
'''

    // URL to the plugin's documentation
    def documentation = "http://github.com/grails-plugins/grails-rxjava"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "OCi", url: "http://www.ociweb.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Graeme Rocher", email: "graeme.rocher@gmail.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "http://github.com/grails-plugins/grails-rxjava/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/grails-plugins/grails-rxjava" ]

    def loadAfter = ['rxMongodb', 'rxGormRestClient']

    Closure doWithSpring() { {->
        rxAsyncResultTransformer(RxResultTransformer)
        rxObservableRenderer(ObservableRenderer)
    }}

}
