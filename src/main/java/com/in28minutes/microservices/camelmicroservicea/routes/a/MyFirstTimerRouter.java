package com.in28minutes.microservices.camelmicroservicea.routes.a;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(name = "routers.first-router.on", matchIfMissing = true, havingValue = "true")
public class MyFirstTimerRouter extends RouteBuilder {
    private final GetCurrentTimeBean getCurrentTimeBean;
    private final SimpleLoggerComponent simpleLoggerComponent;

    public MyFirstTimerRouter(GetCurrentTimeBean getCurrentTimeBean, SimpleLoggerComponent simpleLoggerComponent) {
        this.getCurrentTimeBean = getCurrentTimeBean;
        this.simpleLoggerComponent = simpleLoggerComponent;
    }

    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        // timer
            // transformation when we want transform the received message body
            // processing when we receive a message from a queue, and we want to do an operation or something which do not make any changes on the body of message
            // we can transform a message using the "transform" method or using "bean" method that return something
            // we can process a message using the "process" method or using "bean" method that do not return
        // log
        // Exchange[ExchangePattern: InOnly, BodyType: null, Body: [Body is null]]
        from("timer:first-timer") // from a queue (in this case is timer queue) -> Exchange[ExchangePattern: InOnly, BodyType: null, Body: [Body is null]]
                // PROCESSING
                .log("Processing 1 >> ${body}") // this is a processing

                // TRANSFORMATIONS
                    // DOC: transform the body of message (look upon, actually is null, to a constant message)
                    //.transform().constant("My first timer")

                    // DOC:look! since we are using a constant transformation, the message that will be printed will have always the same Time value
                    //.transform().constant("Now is: " + LocalTime.now())

                    // DOC: look! the bean name must starts with a lower case letter
                    // Spring will look thus on using its Component Scan
                    // this a bad-practice. look the second example
                    //.bean("getCurrentTimeBean")

                    // DOC: we do the same but using injected bean
                    //.bean(getCurrentTimeBean)

                    // DOC: we do the same but using injected bean and specifying the method name
                    .bean(getCurrentTimeBean, "getCurrentTime")
                .log("Processing 2 >> ${body}") // this is a processing
                .bean(simpleLoggerComponent, "doLog") // also this one is a processing because the invoked method does not return anything
                .log("Processing 3 >> ${body}") // this is a processing
                .process(new SimpleLoggerProcessor())

                .to("log:first-timer"); // database
    }
}

/**
 * this is a processor
 */
class SimpleLoggerProcessor implements org.apache.camel.Processor {
    private static final Logger logger = getLogger(SimpleLoggerProcessor.class);

    /**
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("SimpleLoggerProcessor {}", exchange.getMessage().getBody());
    }
}

/**
 * since its method return something it is a Transformer
 */
@Component
class GetCurrentTimeBean {
    public String getCurrentTime() {
        return "Now is: "+LocalTime.now();
    }
}

/**
 * since its method doesn't return anything it is a Processor
 */
@Component
class SimpleLoggerComponent {
    Logger logger = getLogger(SimpleLoggerComponent.class);

    public void doLog(String message) {
        logger.info("SimpleLoggerComponent {}", message);
    }
}