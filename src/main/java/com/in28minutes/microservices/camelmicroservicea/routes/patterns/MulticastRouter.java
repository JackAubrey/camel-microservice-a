package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.patterns.multicast.on", havingValue = "true")
public class MulticastRouter extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("timer:multicast-timer?period=10000")
                .multicast().parallelProcessing()
                .to("direct:first")
                .to("direct:second")
                .to("direct:third")
                .end()
                .to("log:final-logger");

        from("direct:first")
                .delay(1500)
                .to("log:first-log");

        from("direct:second")
                .delay(3000)
                .to("log:second-log");

        from("direct:third")
                .delay(4500)
                .to("log:third-log");
    }
}
