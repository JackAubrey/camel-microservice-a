package com.in28minutes.microservices.camelmicroservicea.routes.best_practices;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.best-practices.on", havingValue = "true")
public class BestPractices extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        systemProperty("I'm...");
        // [BestPractice TRACING & LOGGING LEVEL]
        // NOTE: define it BEFORE the various "from"
        // this line permit to trace camel operation
        // you can also increase debugging level using the spring logging.xxxx property
        // taker a look to the commented line into the application.properties
//        getContext().setTracing(true);

        // [BestPractice DeadLetter Queue]
        // NOTE: define it BEFORE the various "from"
        // in order to ensure no message will be lost
        // we can configure the DLQ
        errorHandler(deadLetterChannel("{{custom.property.dql.channel}}"));

        // [BestPractice Dynamic Properties]
        // we are able to introduce dynamic behavior using the spring properties
        // we can define a property in the application.properties and refer to this one
        // rounding it with a double square brackets
        // look {{custom.property.bp.timer.period}} and {{custom.property.bp.log-endpoint}}
        from("timer:best-practice-timer?period={{custom.property.bp.timer.period}}")

                // [BestPractice WireTap]
                // WireTap is the capability to send the same message to another end-point
                // I think like a multiplex. Copy the message to another channel and continue the pipeline
                // >> fire and forget style << The Exchange is copied in a separated thread
                // Beware only the exchange not the message.
                // if you want also copy the message, you must use the "onPrepare"
                .wireTap("direct:my-new-one-endpoint")
                .to("{{custom.property.bp.log-endpoint}}");

        from("direct:my-new-one-endpoint")
                .delay(5000)
                .transform(simple("Fired Event at ${header.CamelMessageTimestamp}"))
                .log("01 - ${body}")
                .transform(simple("${header.CamelMessageTimestamp}"))
                .log("02 - ${body}")
                .transform(simple("${header.CamelMessageTimestamp} is fired"))
                .log("03 - ${body}")
                .to("log:my-new-one-logger");
    }
}
