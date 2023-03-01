package com.in28minutes.microservices.camelmicroservicea.routes.a;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
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
        // transformation
        // processing when we receive a message from a queue, and we want to do an operation or something which do not make any changes on the body of message
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

                .to("log:first-timer"); // database
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
    Logger logger = LoggerFactory.getLogger(SimpleLoggerComponent.class);

    public void doLog(String message) {
        logger.info("SimpleLoggerComponent {}", message);
    }
}