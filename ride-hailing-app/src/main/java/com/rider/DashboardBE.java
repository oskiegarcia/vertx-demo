package com.rider;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Consumes data from TaxiDriver, filter them and then publish to websocket listeners
 */
public class DashboardBE extends AbstractVerticle {

    private ConcurrentMap<String, JsonObject> drivers = new ConcurrentHashMap<>();
    private AtomicInteger websocketConnCounter = new AtomicInteger();

    @Override
    public void start() throws Exception {
        super.start();


        Router router = Router.router(vertx);

        router.get("/").handler(rc -> {
            rc.response().putHeader("content-type", "text/html")
                    .end("Welcome to Driver Dashboard API Service");
        });

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addOutboundPermitted(new PermittedOptions().setAddress(Config.DASHBOARD_UI_ADDRESS));
        sockJSHandler.bridge(options, event ->{
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.printf("A socket was CREATED: Open Connections = %s\n", websocketConnCounter.incrementAndGet());
            }else if (event.type() == BridgeEventType.SOCKET_CLOSED){
                System.out.printf("A socket was CLOSED: Open Connections = %s\n", websocketConnCounter.decrementAndGet());
            }

            event.complete(true);
        });
        router.route("/eventbus/*").handler(sockJSHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(config().getInteger("http.port", 9080))
                .doOnSuccess(server -> System.out.println(">>>>HTTP server started, listening at port 9080"))
                .doOnError(t -> System.out.println("HTTP server failed to start"))
                .toCompletable()
                .subscribe();


        //consume data from taxi driver then publish to UI dashboard
        consumeAndPublish();



    }

    private void consumeAndPublish() {
        System.out.printf("DashboardBE.consumeAndPublish : listening at address %s\n", Config.DASHBOARD_BE_ADDRESS);
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(Config.DASHBOARD_BE_ADDRESS);

        consumer.handler(m -> {
            JsonObject data = JsonObject.mapFrom(m.body());

            //create and send reply
            JsonObject reply = new JsonObject();
            reply.put("status","OK").put("uuid",data.getValue("uuid"));
            m.reply(reply);

            drivers.put(data.getString("name"), data);

            //five seconds from now
            Instant lastFiveSecs = new Timestamp(System.currentTimeMillis()).toInstant().minusSeconds(5);

            JsonObject jsonOut = new JsonObject();

            //get the data that were updated in the last 5 secs
            List filtered =drivers.values().stream()
                          .filter(v -> v.getInstant("timestamp").isAfter(lastFiveSecs) )
                          .collect(Collectors.toList());

            jsonOut.put("drivers",new JsonArray(filtered));

            //send to dashboard UI
            vertx.eventBus().publish(Config.DASHBOARD_UI_ADDRESS, jsonOut);

        });
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println(">>>>>DashboardBE.stop");
    }
}
