package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.file.xml.2.activemq-router.on", havingValue = "true")
public class FileXmlToActiveMqSenderRouter  extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/json?move=.done&includeExt=xml")
                .log("${body}")
                .to("activemq:xml-activemq-queue");
    }
}
