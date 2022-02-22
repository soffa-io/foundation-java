package com.company.app;

import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import com.company.app.operations.SendEmailHandler;
import io.soffa.foundation.application.context.DefaultRequestContext;
import io.soffa.foundation.application.messages.Message;
import io.soffa.foundation.application.messages.MessageFactory;
import io.soffa.foundation.infrastructure.pubsub.PubSubClientFactory;
import io.soffa.foundation.infrastructure.pubsub.PubSubMessenger;
import io.soffa.foundation.models.mail.Email;
import io.soffa.foundation.models.mail.EmailAddress;
import io.soffa.foundation.models.mail.EmailId;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "app.pubsub.enabled=true",
    "app.pubsub.clients.default.addresses=${PUBSUB_ADDRESSES:simple}",
    "app.pubsub.clients.default.subjects=sample,foundation-service*"
})
@ActiveProfiles("test")
public class PubSubTest {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private PubSubMessenger messenger;

    @SneakyThrows
    @Test
    public void testPubSub() {
        assertNotNull(messenger);
        // Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> pubSubClient.isReady());

        AtomicLong counter = new AtomicLong(SendEmailHandler.COUNTER.get());
        Message event = MessageFactory.create(
            "SendEmail",
            new Email(
                "Hello world",
                EmailAddress.of("to@email.com"),
                "Text message",
                "<h1>Html message</h1>"
            )
        );

        EmailId response = messenger.request(applicationName, event, EmailId.class).get(20, TimeUnit.SECONDS);
        assertNotNull(response);
        assertEquals("000", response.getId());
        assertEquals(counter.incrementAndGet(), SendEmailHandler.COUNTER.get());

        messenger.broadcast("foundation-service", event);
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> SendEmailHandler.COUNTER.get() == counter.incrementAndGet());

        messenger.broadcast("*", event);
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> SendEmailHandler.COUNTER.get() == counter.incrementAndGet());

        Ping ping = PubSubClientFactory.of(Ping.class, applicationName, messenger);
        PingResponse resp = ping.handle(new DefaultRequestContext());
        assertEquals("PONG", resp.getValue());

        /*
        API api = pubSubClient.createClient(API.class, applicationName);
        resp = binaryAPI.ping(new RequestContext());
        assertEquals("PONG", resp.getValue());
        */
    }


}
