package org.mini_lab.personal_cloud_sync.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@ConfigurationPropertiesScan
class RClonePropertiesTest {
    @Autowired
    RCloneProperties rCloneProperties;

    @Test
    void shouldBindTimeoutSecondFromEnvironment() {
        assertEquals(5, rCloneProperties.getTimeOutSecond());
    }
}