package com.in28minutes.microservices.camelmicroservicea.routes.b;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "routers.file-router.on", matchIfMissing = true, havingValue = "true")
public class MyFileRouter extends RouteBuilder {

    /**
     *
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/input?move=.done")
                .log("${body}")
                .to("file:files/output");
    }
}
