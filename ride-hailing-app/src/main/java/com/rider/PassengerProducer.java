package com.rider;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class PassengerProducer extends AbstractVerticle {

     private AtomicInteger counter = new AtomicInteger();
     private AtomicInteger failedCounter = new AtomicInteger();

    @Override
    public void start() throws Exception{

        System.out.println("PassengerProducer.start");
        super.start();

        DeliveryOptions deliveryOptions;
        deliveryOptions = new DeliveryOptions().setSendTimeout(5000); //5 sec
        vertx.setPeriodic(1000, x -> {
            int count = counter.getAndIncrement();
            vertx.eventBus()
                    .<String>rxSend(Config.PASSENGER_PRODUCER_ADDRESS, "Passenger-" + count, deliveryOptions)
                    .retry(5)
                    .map(Message::body)
                    .subscribe(m -> System.out.printf("Received reply \"%s\"\n", m),
                            e -> { System.out.printf("Gave up trying: %s\n",e.getMessage());
                                   failedCounter.incrementAndGet();
                     });
        });



    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println(">>>>PassengerProducer.stop");
    }
}
