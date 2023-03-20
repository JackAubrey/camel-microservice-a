package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.kafka-router.on", havingValue = "true")
public class KafkaSenderRouter extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/kafka?move=.done")
                .log("Sending ${body} to kafka topic")
                .to("kafka:myKafkaTopic");
    }
}
