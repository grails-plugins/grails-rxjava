package rxjava.demo

import rx.Subscriber
import grails.rx.web.*
/**
 * Created by graemerocher on 28/07/2016.
 */
class TickTockController implements RxController {

    def index() {
        rx.stream { Subscriber subscriber ->
            for(i in (0..5)) {
                if(i % 2 == 0) {
                    subscriber.onNext(
                        rx.render("Tick")
                    )
                }
                else {
                    subscriber.onNext(
                        rx.render("Tock")
                    )

                }
                sleep 1000
            }
            subscriber.onCompleted()
        }
    }
}
