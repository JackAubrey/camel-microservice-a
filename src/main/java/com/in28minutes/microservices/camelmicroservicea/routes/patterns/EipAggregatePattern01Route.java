package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import com.in28minutes.microservices.camelmicroservicea.bo.CurrencyExchangeBean;
import com.in28minutes.microservices.camelmicroservicea.routes.patterns.strategies.AggregationListStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.patterns.aggregate.01.on", havingValue = "true")
public class EipAggregatePattern01Route extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        // [EIP: Aggregation]
        // what we are going to do will be:
        // 1: take json files
        // 2: unmarshall
        // 3: aggregate by their "to" attribute
        // 4: every 3 aggregate item, send a single message to the endpoint
        from("file:files/json?move=.done&includeExt=json")
                .unmarshal().json(JsonLibrary.Jackson, CurrencyExchangeBean.class)
                .log("Before aggregate: ${body}")
                .aggregate(simple("${body.to}"), new AggregationListStrategy())
                .completionSize(3)
                .log("After aggregation: ${body}")
                .to("log:aggregation-log");
    }
}