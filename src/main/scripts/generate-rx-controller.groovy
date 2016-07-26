import org.grails.cli.interactive.completers.DomainClassCompleter

description( "Generates a Rx controller that performs CRUD operations" ) {
    usage "grails generate-rx-controller [DOMAIN CLASS]"
    completer DomainClassCompleter
    flag name:'force', description:"Whether to overwrite existing files"
}


if(args) {
    def classNames = args
    if(args[0] == '*') {
        classNames = resources("file:grails-app/domain/**/*.groovy")
                .collect { className(it) }
    }
    for(arg in classNames) {
        def sourceClass = source(arg)
        def overwrite = flag('force') ? true : false
        if(sourceClass) {
            def model = model(sourceClass)
            render template: template('scaffolding/rx/Controller.groovy'),
                    destination: file("grails-app/controllers/${model.packagePath}/${model.convention('Controller')}.groovy"),
                    model: model,
                    overwrite: overwrite

            addStatus "Scaffolding completed for ${projectPath(sourceClass)}"
        }
        else {
            error "Domain class not found for name $arg"
        }
    }
}
else {
    error "No domain class specified"
}
