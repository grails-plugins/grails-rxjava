package rxjava.demo

import static grails.rx.web.Rx.*
import rx.Subscriber

import static grails.async.Promises.*
import rx.Observable

/**
 * Created by graemerocher on 28/07/2016.
 */
class TickTockController {

    def index() {
        def observable = Observable.create({ Subscriber subscriber ->
            task {
                for(i in (0..5)) {
                    if(i % 2 == 0) {
                        subscriber.onNext(
                            render("Tick")
                        )
                    }
                    else {
                        subscriber.onNext(
                            render("Tock")
                        )

                    }
                    sleep 1000
                }
                subscriber.onCompleted()
            }
        } as Observable.OnSubscribe)

        stream observable
    }
}
