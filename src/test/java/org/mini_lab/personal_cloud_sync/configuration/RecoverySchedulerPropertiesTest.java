package org.mini_lab.personal_cloud_sync.configuration;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ConfigurationPropertiesScan
class RecoverySchedulerPropertiesTest extends AbstractIntegrationTest {
    @Autowired
    RecoverySchedulerProperties recoverySchedulerProperties;

    @Test
    void createExecutorConfigBean_shouldReturnBeanFromSpringContext() {
        var context = new AnnotationConfigApplicationContext(RecoverySchedulerProperties.class);
        assertNotNull(context.getAliases("recovery-scheduler"));
    }

    @Test
    void shouldBindPropertiesFromEnvironment() {
        assertEquals(20, recoverySchedulerProperties.getRunningJobTimedOutLimit());
    }

}