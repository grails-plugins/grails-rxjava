package rxjava.demo

import grails.events.*

class MyJob implements EventPublisher {
    static triggers = {
        simple name: 'mySimpleTrigger', startDelay: 1000, repeatInterval: 1000
    }
    def group = "MyGroup"
    def description = "Example job with Simple Trigger"

    static int i = 0

    static final String EVENT_NAME = "MyJob"

    def execute(){
        log.trace('MyJob.execute()')
        notify(EVENT_NAME, "${i++}")
    }
}
