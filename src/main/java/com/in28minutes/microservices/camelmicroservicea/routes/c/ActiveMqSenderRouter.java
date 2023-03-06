package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "routers.activemq-router.on", matchIfMissing = false, havingValue = "true")
public class ActiveMqSenderRouter extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("timer:active-mq-timer?period=10000")
                .transform().constant("My message for Active MQ")
                .log("${body}")
                // NOTE: queues are not imported by default. you must add the dependency camel-activemq-starter
                .to("activemq:my-activemq-queue");
    }
}
