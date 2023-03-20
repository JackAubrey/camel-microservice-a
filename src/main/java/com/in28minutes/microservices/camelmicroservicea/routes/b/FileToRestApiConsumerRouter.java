package com.in28minutes.microservices.camelmicroservicea.routes.b;

import com.in28minutes.microservices.camelmicroservicea.bo.CurrencyExchangeBean;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "routers.file-to-rest-router.on", havingValue = "true")
public class FileToRestApiConsumerRouter extends RouteBuilder {
    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        restConfiguration().host("localhost").port(8081);

        from("file:files/input?move=.done")
                .routeId("Files-To-Rest-Router")
                //.transform().body(String.class) <<- not required! is just for documentation: if you would like SimpleLanguage something like ${body} contains '...'
                .choice()
                    .when(simple("${file:ext} ends with 'json'"))
                        .log("${body}")
                        .unmarshal().json(JsonLibrary.Jackson, CurrencyExchangeBean.class)
                        .setHeader("from", simple("${in.body.from}") )
                        .setHeader("to", simple("${in.body.to}") )
                        .to("rest:get:/currency/from/{from}/to/{to}")
                        .log("Response from JSON value ${body}")
                        .endChoice()
                    .when(simple("${file:ext} ends with 'xml'"))
                        .log("before unmarshalling ${body}")
                        .unmarshal().jacksonXml(CurrencyExchangeBean.class)
                        .log("after unmarshalling ${body}")
                        .setHeader("from", simple("${in.body.from}") )
                        .setHeader("to", simple("${in.body.to}") )
                        .to("rest:get:/currency/from/{from}/to/{to}")
                        .log("Response from XML value ${body}")
                        .endChoice()
                    .otherwise()
                        .log("The file ${body} not sent to REST End-Point")
                .end()
                .log("${messageHistory}");
    }
}
