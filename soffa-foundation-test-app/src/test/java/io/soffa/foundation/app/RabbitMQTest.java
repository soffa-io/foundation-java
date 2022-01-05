package io.soffa.foundation.app;

import io.soffa.foundation.events.Event;
import io.soffa.foundation.pubsub.PubSubClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "app.amqp.enabled=true",
    "app.amqp.clients.default=amqp://guest:guest@localhost:5672",
})
public class RabbitMQTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private PubSubClient pubSubClient;

    @SneakyThrows
    @Test
    public void testRabbitMQ() {
        Assertions.assertNotNull(pubSubClient);
        pubSubClient.sendInternal(new Event("HELLO1"));
        pubSubClient.broadcast(new Event("HELLO2"));
        TestPubSubListener.LATCH.await();
        assertEquals(0, TestPubSubListener.LATCH.getCount());
    }

}
