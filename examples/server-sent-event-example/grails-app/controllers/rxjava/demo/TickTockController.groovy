package rxjava.demo

import grails.converters.JSON
import grails.rx.web.*
import reactor.spring.context.annotation.Consumer
import reactor.spring.context.annotation.Selector
import rx.Observable
import rx.Subscriber
import rx.subjects.*
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by graemerocher on 28/07/2016.
 */
@Consumer
class TickTockController implements RxController {

    def index() {
        rx.stream { Subscriber emitter ->
            for (i in (0..5)) {
                if (i % 2 == 0) {
                    emitter.onNext(
                            rx.render("Tick")
                    )
                } else {
                    emitter.onNext(
                            rx.render("Tock")
                    )

                }
                sleep 1000
            }
            emitter.onCompleted()
        }
    }

    def sse() {
        def lastId = request.getHeader('Last-Event-ID') as Integer
        def startId = lastId ? lastId + 1 : 0
        log.info("Last Event ID: $lastId")
        rx.stream { Subscriber emitter ->
            log.info("SSE Thread ${Thread.currentThread().name}")
            for (i in (startId..(startId + 9))) {
                if (i % 2 == 0) {
                    emitter.onNext(
                            rx.event("Tick\n$i", id: i, event: 'tick', comment: 'tick')
                    )
                } else {
                    emitter.onNext(
                            rx.event("Tock\n$i", id: i, event: 'tock', comment: 'tock')
                    )

                }
                sleep 1000
            }
            emitter.onCompleted()
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
                def json = [type: 'observable', num: id] as JSON

                rx.event(new Writable() {
                    @Override
                    Writer writeTo(Writer writer) throws IOException {
                        json.render(writer)
                        return writer
                    }
                }, id: id, comment: 'hello')
            }
            .take(10)
        )
    }

    Subject subject = PublishSubject.create()
    Observable publishedObservable = subject.publish().autoConnect().observeOn(Schedulers.io())

    @Selector('MyJob.event')
    void myEventListener(Object data) {
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

                def json = [type: 'quartz', num: it as int] as JSON
                rx.event(new Writable() {
                    @Override
                    Writer writeTo(Writer writer) throws IOException {
                        json.render(writer)
                        return writer
                    }
                }, comment: 'hello')
            }
        )
    }
}
