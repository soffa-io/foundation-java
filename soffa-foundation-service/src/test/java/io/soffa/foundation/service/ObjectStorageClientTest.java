package io.soffa.foundation.service;

import io.soffa.foundation.infrastructure.storage.ObjectStorageClient;
import io.soffa.foundation.infrastructure.storage.model.ObjectStorageConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "app.s3.enabled=true",
    "app.s3.endpoint=https://foo",
    "app.s3.accessKey=key",
    "app.s3.secretKey=secret",
    "app.s3.bucket=bucket",
})
@ActiveProfiles("test")
public class ObjectStorageClientTest {

    @Autowired
    private ObjectStorageClient client;

    @Autowired
    private ObjectStorageConfig config;

    @Test
    public void testObjectStorageClient() {
        assertNotNull(config);
        assertEquals("https://foo", config.getEndpoint());
        assertEquals("key", config.getAccessKey());
        assertEquals("secret", config.getSecretKey());
        assertEquals("bucket", config.getBucket());
        assertNotNull(client);
    }

}
