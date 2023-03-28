package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import com.in28minutes.microservices.camelmicroservicea.routes.patterns.strategies.AggregationListStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public EipPatternRouter(SplitterBeanExpression splitterBeanExpression, SplitterBeanComponent splitterBeanComponent, OnlyPairIntValueFilter onlyIntValueFilter) {
        this.splitterBeanExpression = splitterBeanExpression;
        this.splitterBeanComponent = splitterBeanComponent;
        this.onlyIntValueFilter = onlyIntValueFilter;
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
        from("timer:multicast-timer?period=3000")
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
                // to better understand this concept, look the history logs.
                //
                // >> if you comment the "multicast" operation you'll obtain <<
                // Source                                   ID                             Processor                                          Elapsed (ms)
                //                                         MultiCastTimerRouteId/MultiCas from[timer://multicast-timer?period=3000]               1121318
                //                                         MultiCastTimerRouteId/transfor transform[constant{A value}]                                  2
                //                                         MultiCastTimerRouteId/to1      log:first-log                                                 2
                //                                         MultiCastTimerRouteId/to2      log:second-log                                                0
                //                                         MultiCastTimerRouteId/to3      log:third-log                                                 0
                //                                         MultiCastTimerRouteId/log1     log                                                           0
                //
                // >> if you enable the "multicast" <<
                // Source                                   ID                             Processor                                          Elapsed (ms)
                //                                         MultiCastTimerRouteId/MultiCas from[timer://multicast-timer?period=3000]               1261830
                //                                         MultiCastTimerRouteId/transfor transform[constant{A value}]                                  3
                //                                         MultiCastTimerRouteId/multicas multicast                                                     0
                //                                         MultiCastTimerRouteId/log1     log                                                           0
                //
                // so this means, if you don't use multicast, ol the child end-points will be execute sequentially
                // if you use "multicast" a copy of source message will be sent to the children at the same time.
                // for example you could send a copy of the source message to: activemq, rest-api end so on at the same time.

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

