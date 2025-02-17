package com.company.app;

import com.google.common.collect.ImmutableMap;
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.test.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationTest {

    static final String F_USERNAME = "username";
    static final String F_PASSWORD = "password";
    static final String CHECK_URI = "/check";

    @Autowired
    private MockMvc mvc;

    @Test
    void testActuator() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/actuator/health")
            .header("Access-Control-Request-Method", "GET")
            .header("Origin", "https://www.someurl.com")
            .expect().isOK().json("$.status", "UP");

        test.get("/health")
            .expect().isOK();

        test.get("/actuator/info")
            .expect().isOK();

        test.get("/actuator/metrics")
            .expect().isOK();

    }

    @Test
    void testController() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/ping").
            header("X-Application", "TestApp").
            header("X-TenantId", "T1").
            header("X-TraceId", IdGenerator.shortUUID("trace-")).
            header("X-SpanId", IdGenerator.shortUUID("span-")).
            expect().isOK().json("$.value", "PONG");


        test.get("/ping")
            .basicAuth("foo", "bar")
            .header("X-Application", "TestApp")
            .header("X-TenantId", "T1")
            .expect().isOK();
    }

    @Test
    void testOpenAPI() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/v3/api-docs")
            .expect().isOK();
    }

    @Test
    void testRedocController() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/")
            .expect().isOK();
    }

    @Test
    void testCustomRestExceptionHandler() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/ping").
            header("X-Application", "TestApp").
            header("X-TenantId", "T2").
            header("X-TraceId", IdGenerator.shortUUID("trace-")).
            header("X-SpanId", IdGenerator.shortUUID("span-")).
            expect().is5xxServerError().
            hasJson("$.timestamp").
            hasJson("$.kind").
            hasJson("$.status").
            hasJson("$.message").
            hasJson("$.prod").
            hasJson("$.traceId").
            hasJson("$.spanId").
            hasJson("$.application");
    }

    @Test
    void testValidation() {
        HttpExpect test = new HttpExpect(mvc);

        test.post(CHECK_URI).
            expect().isBadRequest();

        test.post(CHECK_URI).
            withJson(ImmutableMap.of(F_USERNAME, "john.doe")).
            expect().isBadRequest();

        test.post(CHECK_URI).
            withJson(ImmutableMap.of(F_USERNAME, "")).
            expect().isBadRequest();


        test.post(CHECK_URI).
            withJson(ImmutableMap.of(F_PASSWORD, "P4ssw0rd")).
            expect().isBadRequest();


        test.post(CHECK_URI).
            withJson(ImmutableMap.of(F_PASSWORD, "")).
            expect().isBadRequest();

        test.post(CHECK_URI).withJson(ImmutableMap.of(
                F_USERNAME, "john.doe",
                F_PASSWORD, ""
            )).
            expect().isBadRequest();

        test.post(CHECK_URI).withJson(ImmutableMap.of(
                F_USERNAME, "",
                F_PASSWORD, "P4ssw0rd"
            )).
            expect().isBadRequest();

        test.post(CHECK_URI).withJson(ImmutableMap.of(
                F_USERNAME, "john.doe",
                F_PASSWORD, "P4ssw0rd"
            )).
            expect().isOK();
    }


}
