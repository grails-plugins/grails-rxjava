package rxjava.demo

import grails.converters.JSON
import grails.rx.web.*
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import grails.events.annotation.*
import java.util.concurrent.TimeUnit

/**
 * Created by graemerocher on 28/07/2016.
 */
class TickTockController implements RxController {

    def index() {
        rx.stream { Emitter emitter ->
            for(i in (0..5)) {
                if(i % 2 == 0) {
                    emitter.onNext(
                        rx.render("Tick")
                    )
                }
                else {
                    emitter.onNext(
                        rx.render("Tock")
                    )

                }
                sleep 1000
            }
            emitter.onComplete()
        }
    }

    def sse() {
        def lastId = request.getHeader('Last-Event-ID') as Integer
        def startId = lastId ? lastId + 1 : 0
        log.info("Last Event ID: $lastId")
        rx.stream { Emitter emitter ->
            log.info("SSE Thread ${Thread.currentThread().name}")
            for(i in (startId..(startId+9))) {
                if(i % 2 == 0) {
                    emitter.onNext(
                            rx.event("Tick\n$i", id: i, event: 'tick', comment: 'tick')
                    )
                }
                else {
                    emitter.onNext(
                            rx.event("Tock\n$i", id: i, event: 'tock', comment: 'tock')
                    )

                }
                sleep 1000
            }
            emitter.onComplete()
        }
    }

    def observable() {
        def lastId = request.getHeader('Last-Event-ID') as Integer
        def startId = lastId ? lastId + 1 : 0
        rx.stream(
                Observable
                        .interval(1, TimeUnit.SECONDS)
                        .doOnSubscribe { log.info("Observable Subscribe Thread ${Thread.currentThread().name}") }
                        .doOnNext { log.info("Observable Thread ${Thread.currentThread().name}") }
                        .map {
                            def id = it + startId
                            rx.event([type: 'observable', num: id] as JSON, id: id, comment: 'hello')
                        }
                        .take(10),
        )
    }

    Subject subject = PublishSubject.create()
    Observable publishedObservable = subject.publish().autoConnect().observeOn(Schedulers.io())

    @Subscriber("MyJob")
    void myEventListener(int data) {
        log.info("myEvent listener Thread ${Thread.currentThread().name}")
        subject.onNext(data)
    }

    def quartz() {
        rx.stream(
                publishedObservable
                        .doOnSubscribe { log.info("Quartz Subscribe Thread ${Thread.currentThread().name}") }
                        .doOnError { log.info("Quartz thread error") }
                        .map {
                            log.info("Quartz Thread ${Thread.currentThread().name}")
                            rx.event([type: 'quartz', num: it as int] as JSON, comment: 'hello')
                        }
        )
    }
}
