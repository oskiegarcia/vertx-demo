package com.rider;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
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
        vertx.setPeriodic(3000, x -> {
            int count = counter.getAndIncrement();
            vertx.eventBus()
                    .<String>rxSend(Config.PASSENGER_PRODUCER_ADDRESS, "Passenger-" + count, deliveryOptions)
                    .retry(5)
                    .map(Message::body)
                    .subscribe(m -> System.out.printf("Received reply \"%s\"\n", m),
                            e -> { int errCode = ((ReplyException)e).failureCode();
                                   System.out.printf("Gave up trying: %d - %s\n",errCode, e.getMessage());
                                   failedCounter.incrementAndGet();
                     });
        });



    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println(">>>>PassengerProducer.stop");
        System.out.printf("Number of passengers not serviced: %d\n", failedCounter.get());
    }
}
