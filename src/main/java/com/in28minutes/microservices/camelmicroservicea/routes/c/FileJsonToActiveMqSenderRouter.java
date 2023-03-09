package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "routers.file.json.2.activemq-router.on", havingValue = "true")
public class FileJsonToActiveMqSenderRouter extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/json?move=.done&includeExt=json")
                .log("${body}")
                // NOTE: queues are not imported by default. you must add the dependency camel-activemq-starter
                .to("activemq:my-activemq-queue");
    }
}
