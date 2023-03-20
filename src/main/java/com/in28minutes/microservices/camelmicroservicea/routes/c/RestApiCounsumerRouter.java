package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(value = "routers.restapi.on", havingValue = "true")
public class RestApiCounsumerRouter extends RouteBuilder {
    private static final List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());
    private static final Random random = new Random();
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        restConfiguration().host("localhost").port(8081);

        from("timer:timer-rest-api-consumer?period=5000&delay=2000")
                .setHeader("from", this::randCurrency)
                .setHeader("to", this::randCurrency)
                .to("rest:get:/currency/from/{from}/to/{to}")
                .log("Exchange received from REST API ${body}");
    }

    private String randCurrency() {
        int pointer = random.nextInt( currencies.size() );
        return currencies.get(pointer).getCurrencyCode();
    }
}
