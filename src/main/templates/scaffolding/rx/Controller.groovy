<%=packageName ? "package ${packageName}" : ''%>

import grails.rx.web.Rx
import grails.validation.ValidationException
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.*
import static grails.rx.web.Rx.*
import static rx.Observable.*

@CompileStatic
class ${className}Controller {

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        zip( ${className}.list(params), ${className}.count() ) { List ${propertyName}List, Number count ->
            render view:"index", model:[${propertyName}List: ${propertyName}List, ${propertyName}Count: count]
        }
    }

    def show() {
        ${className}.get((Serializable)params.id)
    }

    def save() {
        Rx.bindData(new ${className}(), request)
                .switchMap { ${className} ${propertyName} ->
            if(${propertyName}.hasErrors()) {
                just(
                    respond( ${propertyName}.errors, view:'create')
                )
            }
            else {
                ${propertyName}.save(flush:true)
                        .map { ${className} b ->
                    respond b, [status: CREATED, view:"show"]
                }
                .onErrorReturn { Throwable e ->
                    if(e instanceof ValidationException) {
                        respond e.errors, view:'create'
                    }
                    else {
                        log.error("Error saving entity: \$e.message", e)
                        return INTERNAL_SERVER_ERROR
                    }
                }
            }

        }
    }

    def update() {
        def request = this.request
        ${className}.get((Serializable)params.id)
                    .switchMap { ${className} ${propertyName} ->
            Rx.bindData( ${propertyName}, request )
                    .switchMap { ${className} updatedBook ->
                !updatedBook.hasErrors()? updatedBook.save() : updatedBook
            }
        }
        .map { ${className} ${propertyName} ->
            if(${propertyName}.hasErrors()) {
                respond ${propertyName}.errors, view:'edit'
            }
            else {
                respond ${propertyName}, [status: OK, view:"show"]
            }
        }
        .switchIfEmpty(
            just( Rx.render(status: NOT_FOUND) )
        )
        .onErrorReturn { Throwable e ->
            if(e instanceof ValidationException) {
                respond e.errors, view:'edit'
            }
            else {
                log.error("Error saving entity: \$e.message", e)
                return INTERNAL_SERVER_ERROR
            }
        }
    }

    def delete() {
        ${className}.get((Serializable)params.id)
                    .switchMap { ${className} ${propertyName} ->
            ${propertyName}.delete()
        }
        .map {
            render status: NO_CONTENT
        }
    }
}
