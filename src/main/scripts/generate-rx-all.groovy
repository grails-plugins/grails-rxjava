import org.grails.cli.interactive.completers.DomainClassCompleter

description( "Generates a Rx controller that performs CRUD operations and the associated views" ) {
  usage "grails generate-rx-all [DOMAIN CLASS]"
  completer DomainClassCompleter
  flag name:'force', description:"Whether to overwrite existing files"
}

generateRxController(*args)
generateViews(*args)
