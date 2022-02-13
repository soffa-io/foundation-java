package com.company.app;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.messages.Message;
import io.soffa.foundation.messages.MessageHandler;
import io.soffa.foundation.metrics.MetricsRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class EventHandleTest {

    @Autowired
    private MessageHandler handler;

    @Autowired
    private MetricsRegistry metricsRegistry;

    private double getCounterValue(String op) {
        return metricsRegistry.counter("app_operation_" + op);
    }

    @Test
    public void testEventsHandler() {
        String ping = Ping.class.getName();
        String echo = Echo.class.getName();

        double pingCount = getCounterValue(ping);
        double echoCount = getCounterValue(Echo.class.getName());

        TenantHolder.use("T1", (t1) -> {
            handler.handle(new Message(ping)); // automatic tenant
            handler.handle(new Message(echo, "Hello"));
            handler.handle(new Message(ping).withTenant(t1)); // explicit tenant
        });

        assertEquals(pingCount + 2, getCounterValue(ping));
        assertEquals(echoCount + 1, getCounterValue(echo));
    }


}
