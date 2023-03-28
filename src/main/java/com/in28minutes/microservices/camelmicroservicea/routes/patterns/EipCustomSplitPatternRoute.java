package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(value = "routers.patterns.split.custom.on", havingValue = "true")
public class EipCustomSplitPatternRoute extends RouteBuilder {
    private final SpliterComponent spliterComponent;

    public EipCustomSplitPatternRoute(SpliterComponent spliterComponent) {
        this.spliterComponent = spliterComponent;
    }

    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("file:files/input?move=.done&includeExt=csv")
                .convertBodyTo(String.class) // converting body to what we want. In this case in a String
                .log("Before Split: ${body}")
                .split(method(spliterComponent)) // this one take its affect jut if you convert into string the body before
                .log("After Split: ${body}")
                .to("log:split-log");
    }
}

@Component
class SpliterComponent {
    public List<String> split(Object value) {
        if(value != null) {
            return List.of( value.toString().split(",") );
        } else {
            return new ArrayList<>();
        }
    }
}
