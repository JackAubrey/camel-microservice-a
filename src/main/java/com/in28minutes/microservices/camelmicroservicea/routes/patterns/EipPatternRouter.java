package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import com.in28minutes.microservices.camelmicroservicea.routes.patterns.strategies.AggregationListStrategy;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Example router which the goal to show and document some Camel Enterprise Integration Patterns (EIP)
 */
@Component
@ConditionalOnProperty(value = "routers.patterns.on", havingValue = "true")
public class EipPatternRouter extends RouteBuilder {
    private final Random random = new Random();
    private final SplitterBeanExpression splitterBeanExpression;
    private final SplitterBeanComponent splitterBeanComponent;
    private final OnlyPairIntValueFilter onlyIntValueFilter;
    private final DynamicRoutingComponent dynamicRoutingComponent;

    public EipPatternRouter(SplitterBeanExpression splitterBeanExpression, SplitterBeanComponent splitterBeanComponent, OnlyPairIntValueFilter onlyIntValueFilter, DynamicRoutingComponent dynamicRoutingComponent) {
        this.splitterBeanExpression = splitterBeanExpression;
        this.splitterBeanComponent = splitterBeanComponent;
        this.onlyIntValueFilter = onlyIntValueFilter;
        this.dynamicRoutingComponent = dynamicRoutingComponent;
    }

    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        // [EIP: Pipeline]
        // by default, starting the "from" we are using the PipelinePattern
        // all the piped command are going to compose our pipeline.
        // a pipeline is a sequence of steps
        from("timer:multicast-timer?period=20000")
                //.pipeline() <- is not necessary because is by default. we are showing just to better understand the pipeline concept

                // not necessary. Jus to have a quick understanding of which route is running
                .routeId("MultiCastTimerRouteId")

                .transform().body(i -> new SimpleBeanMessage(random.nextInt(100)))

                // [EIP: Content Based Routing Pattern]
                // typically is choice
                // this pattern analyze headers, bean, body or whatever you want to take a decision.
                // Decision based on Content to route to other endpoint.
                // in this very simple case, if the bean contains a pair or odd number will be logged by a specific log endpoint.
                .choice()
                    .when(b -> b.getMessage().getBody(SimpleBeanMessage.class).isPair())
                        .log("${body} is pair")
                    .otherwise()
                        .log("${body} is odd")
                .end()

                // [EIP: Split Pattern]
                // look on EipSplitPatternRoute to more appropriated example
                // split create multiple message starting from one and propagated to the children
                // we have implemented a simple expression class that take the input SimpleBean
                // and return a List of string made with splitting its message by "random" reg-ex word
                // and also adding to the list the bean value.
                // So this means that a body message like this
                //      SimpleBeanMessage{message='A new random value', value=1234}
                // Body will be split in 3 parts ["A new", "value", "1234"]
                // Every part going to a message iterated and sent to child end-points
                //
                // >> take a look to the obtained log <<
                // * Before Split SimpleBeanMessage{message='A new random value', value=6}
//                .log("Before Split: ${body}")
//                .split(splitterBeanExpression)
                .split(method(splitterBeanComponent))// you can do the same in the easiest manner
                // * First iteration -> After Split: A new
                // * Second iteration -> After Split:  value
                // * Third iteration -> After Split: 6
//                .log("After Split: ${body}")
                // So since we are going to use a multicast for each iteration will obtain
                // * First Iteration
                //      first-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body: A new ]
                //      second-log                               : Exchange[ExchangePattern: InOnly, BodyType: String, Body: A new ]
                //      third-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body: A new ]
                // * Second Iteration
                //      first-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  value]
                //      second-log                               : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  value]
                //      third-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  value]
                // * Third Iteration
                //      first-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  6]
                //      second-log                               : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  6]
                //      third-log                                : Exchange[ExchangePattern: InOnly, BodyType: String, Body:  6]

                // [EIP: Multicast]
                // here we can se another EIP: Multicast!!
                // multicast EIP send A COPY of the original message to all its child output
                // in the example below the messages will be multicasted in a separated threads
                // the "end()" permit to use to wait the end of X, Y, Z endpoint before to proceed to the next step
                // .multicast().parallelProcessing()
                //    .to("direct:x")
                //    .to("direct:y")
                //    .to("direct:z")
                //  .end()
                //  .to("mock:result")

                // [EIP: Aggregation]
                // this patter is the opposite of Split one
                // message will be aggregate by a term condition and sent as single message to the end-points
                // taking a look to what we documented upon, the bean message has been split in tre parts, 2 fixed and one variable.
                //  ["A new", "value", X]
                // where the X is the dynamic value generated for each timer tick.
                // now we are going to aggregate by pair values
                // TAKE A LOOK to EipAggregatePattern01Route to see a more appropriated example
                //.filter().method(onlyIntValueFilter)
                // 0 - remember that the source inline message has been split
                //      this means:
                //       - the source bean: SimpleBeanMessage{message='A new random value', value=1234}
                //       - wil be transformed by our custom split component in a LIST ['A new', 'value, '1234', 'pair']
                //       - all the single list items will be used as single message
                // 1 - we are filtering split message in order to take only those that are equals to 'pair'
                // 2 - aggregate every single body message received from the filter
                // 3 - if we found 3 message equals to 'pair' we produce a single aggregation message
                .filter(simple("${body} == 'pair'"))
                    .aggregate(simple("${body}"), new AggregationListStrategy())
                    .completionSize(3) // here we are specifying that every 3 messages we have aggregate, we must send one
                    .to("log:first-log"/*, "log:second-log", "log:third-log"*/)
                .end();

        // [EIP: Routing Slip]
        // this is similar to multicast but patter permit us to dynamically choose where route
        // try to play with the list of endpoints: if we use just endpoint1 and endpoint3, only these will be invoked
        String endpoint1 = "direct:routSlip01";
        String endpoint2 = "direct:routSlip02";
        String endpoint3 = "direct:routSlip03";
        String routingSlipEndpoints = List.of(endpoint1, endpoint2, endpoint3).stream().collect(Collectors.joining(","));
        from("timer:routing-slip-timer?period=30000")
                .transform(simple("Fired Event at ${header.CamelMessageTimestamp}"))
                .log("${body}")
                .routingSlip(simple(routingSlipEndpoints));

        // NOTE: here we are showing the properties usage also.
        // is possible to refer to the properties defined in the application.properties putting them in a double square bracket
        // look "routing-slip-time-period-property" and "endpoint-logging-01-property". both are simple examples of this usage
        // [EIP: Dynamic Routing]
        // this one is similar to the RoutingSlip but call an end-point by a logic and continue to do this until it receive a NULL
        from("timer:routing-slip-timer?period={{routing-slip-time-period-property}}")
                .transform(simple("Fired Event at ${header.CamelMessageTimestamp}"))
                        .dynamicRouter(method(dynamicRoutingComponent));

        // these 3 "direct" endpoint will be dynamically invoked by the router-slip or by the DynamicRouter
        from(endpoint1)
                // here you can see end-point logging configured over application.properties
                // take a look to the form: the property name is double rounded by a square brackets
                .to("{{endpoint-logging-01-property}}");

        from(endpoint2)
                .to("log:log-routing-slip-02");

        from(endpoint3)
                .to("log:log-routing-slip-03");
    }
}

@Component
class OnlyPairIntValueFilter {
    public boolean filterOnlyInt(Object value) {
        if(value == null) {
            return false;
        }  else {
            try {
                Integer.parseInt(value.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}

class SimpleBeanMessage {
    private String message = "A new random value";
    private int value;

    private boolean pair;

    public SimpleBeanMessage(int value) {
        this.value = value;
        this.pair = this.value % 2 == 0;
    }

    public boolean isPair() {
        return pair;
    }

    public String getMessage() {
        return message;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SimpleBeanMessage{" +
                "message='" + message + '\'' +
                ", value=" + value +
                '}';
    }
}

@Component
class SplitterBeanExpression implements Expression {
    private final SplitterBeanComponent splitterBeanComponent;

    SplitterBeanExpression(SplitterBeanComponent splitterBeanComponent) {
        this.splitterBeanComponent = splitterBeanComponent;
    }

    /**
     * @param exchange
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        SimpleBeanMessage bean = exchange.getMessage().getBody(SimpleBeanMessage.class);
        List<String> result = splitterBeanComponent.splitBean(bean);
        return exchange.getContext().getTypeConverter().convertTo(type, exchange, result);
    }
}


@Component
class SplitterBeanComponent {
    public List<String> splitBean(SimpleBeanMessage bean) {
        String [] split = bean.getMessage().split("\\brandom\\b");
        List<String> result = new ArrayList<>(List.of(split));
        result.add(""+bean.getValue());
        result.add(""+(bean.isPair() ? "pair" : "odd"));
        return result;
    }
}

@Component
class DynamicRoutingComponent {
    private int count = 0;
    private static final String endpoint1 = "direct:routSlip01";
    private static final String endpoint2 = "direct:routSlip02";
    private static final String endpoint3 = "direct:routSlip03";

    private static final List<String> paths = List.of(
            endpoint1,
            endpoint2,
            endpoint2 +"," + endpoint1,
            endpoint1 +"," + endpoint3,
            endpoint1 +"," + endpoint3 +"," + endpoint3,
            endpoint3
    );
    private static final Logger logger = getLogger(DynamicRoutingComponent.class);

    // we show here in this little example some parameter we want to be injected in order to take our decision
    public String decideTheNextEndpoint(@Body String body,
                                        @Headers Map<String, Object> headers,
                                        @ExchangeProperties Map<String, Object> exProperties) {
        logger.info("Invoked Counter = {}", count);
        String endPoints = null;
        if(count < paths.size()) {
            endPoints = paths.get(count);
            count++;
        } else {
            count = 0;
        }
        return endPoints;
    }
}