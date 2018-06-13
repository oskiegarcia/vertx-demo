package com.rider;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import org.jgroups.util.UUID;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaxiDriver extends AbstractVerticle {

    private AtomicInteger counter = new AtomicInteger();
    private AtomicBoolean occupied = new AtomicBoolean(false);

    private String driverName;

    @Override
    public void start() throws Exception {
        super.start();
        driverName=System.getProperty("driver");

        System.out.printf("TaxiDriver.start for listening address %s\n", Config.PASSENGER_PRODUCER_ADDRESS);

        MessageConsumer<String> consumer = vertx.eventBus().consumer(Config.PASSENGER_PRODUCER_ADDRESS);
        consumer.handler(m -> {
            System.out.printf("received %s from %s\n", m.body(), Config.PASSENGER_PRODUCER_ADDRESS);
            String from = String.format("[From: %s]",driverName);

            if (occupied.get()) {
                String failMsg = String.format("%s Sorry %s ,I'm not available at the moment.",from, m.body());
                m.fail(500, failMsg);
            } else { //pick up the new passenger
                counter.incrementAndGet();
                occupied.set(true);
                m.reply(String.format("%s Welcome %s", from,  m.body()) );
                sendToDashboardBE();

                //free after 5 sec, simulate passenger being dropped off
                vertx.setTimer(5000, x ->{
                    occupied.set(false);
                    sendToDashboardBE();
                });

            }

        });







    }

    private void sendToDashboardBE() {

        DeliveryOptions deliveryOptions;
        deliveryOptions = new DeliveryOptions().setSendTimeout(30000); //30 sec

        JsonObject jsonObject = toJson();
        vertx.eventBus().<JsonObject>rxSend(Config.DASHBOARD_BE_ADDRESS, jsonObject, deliveryOptions)
                .retry(3)
                .map(Message::body)
                .subscribe(msg -> System.out.printf("Received reply \"%s\" from %s\n", msg, Config.DASHBOARD_BE_ADDRESS),
                        e -> System.out.printf("Gave up trying: %s\n",e.getMessage()));
    }

    private JsonObject toJson() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("name", driverName)
                  .put("occupied",occupied.get())
                  .put("passengerCount",counter.get())
                  .put("timestamp",timestamp.toInstant())
                  .put("uuid", UUID.randomUUID().toString());

         return jsonObject;

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println(">>>>>TaxiDriver.stop");
    }
}
