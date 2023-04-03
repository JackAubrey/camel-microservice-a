package com.in28minutes.microservices.camelmicroservicea.routes.enc;

import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.crypto.CryptoDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@ConditionalOnProperty(value = "routers.activemq-router.enc.on", havingValue = "true")
public class ActiveMqEncSenderRoute extends RouteBuilder {
    private static final Logger logger = getLogger(ActiveMqEncSenderRoute.class);

    /**
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {
        from("timer:send-enc-scheduled-message?period=2000")
                .bean(CustomMessageProducer.class)
                .marshal().json(JsonLibrary.Jackson)
                .log("Clear Body = ${body}")
                .marshal(createEncryptor())
                .to("activemq:enc-queue");
    }

    private CryptoDataFormat createEncryptor() throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        ClassLoader classLoader = getClass().getClassLoader();
        keyStore.load(classLoader.getResourceAsStream("myDesKey.jceks"), "Ringhietto2009".toCharArray());
        Key sharedKey = keyStore.getKey("myDesKey", "Ringhietto2009".toCharArray());

        CryptoDataFormat sharedKeyCrypto = new CryptoDataFormat("DES", sharedKey);
        logger.info("The KeyStore has been loaded");
        return sharedKeyCrypto;
    }
}

@Component
class CustomMessageProducer {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'mm:dd:ss.X");
    public Map<String, Object> produce(@Header("CamelMessageTimestamp") long timeStamp) {
        Map<String, Object> map = new HashMap<>();
        map.put("Title", "Custom Message");
        map.put("Created", formatter.format(new Date(timeStamp)));
        return map;
    }
}
