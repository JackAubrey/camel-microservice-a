package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.patterns.split.on", havingValue = "true")
public class EipSplitPatternRoute extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/input?move=.done")
                .choice()
                    .when(simple("${file:ext} == 'csv'"))
                        .unmarshal().csv()
                    .endChoice()
                    .otherwise()
                        .stop()
                .end()
                // every single item will be sent as single message
                // .log("Before ${body}")
                // .convertBodyTo(String.class) <- since the body is an ArryList, without this one the split by comma would not affect.
                .log("Before Split: ${body}")
                //.split(body(), ",") <-- this one take its affect jut if you convert into string the body before
                .split(body()) //
                .log("After Split: ${body}")
                .marshal().json(JsonLibrary.Jackson, String.class)
                .multicast()
                // multicast going to send the split value to both active-mq and log end-points
                .to("activemq:split-queue", "log:split-log");

        from("activemq:split-queue")
                .to("log:dequeued-message");
    }
}
