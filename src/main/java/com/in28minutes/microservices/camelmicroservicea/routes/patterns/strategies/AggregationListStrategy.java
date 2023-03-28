package com.in28minutes.microservices.camelmicroservicea.routes.patterns.strategies;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class AggregationListStrategy implements AggregationStrategy {
    /**
     * @param oldExchange
     * @param newExchange
     * @return
     */
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // 1,2,3,...,x
        // T0 - oldExchange = null, we return 1 in newExchange
        // T1 - oldExchange = 1     we return 1,2 in newExchange
        // T2 - oldExchange = 1,2   we return 1,2,3 in newExchange
        // and so on
        Object newBody = newExchange.getIn().getBody();

        if (oldExchange == null) {
            List<Object> list = new ArrayList<>();
            list.add(newBody);
            newExchange.getIn().setBody(newBody);
            return newExchange;
        } else {
            List<Object> list = oldExchange.getIn().getBody(ArrayList.class);
            list.add(newBody);
            return oldExchange;
        }
    }
}
