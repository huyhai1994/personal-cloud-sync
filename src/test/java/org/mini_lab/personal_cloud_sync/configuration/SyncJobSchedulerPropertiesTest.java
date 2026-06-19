package org.mini_lab.personal_cloud_sync.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ConfigurationPropertiesScan
class SyncJobSchedulerPropertiesTest {
    @Autowired
    SyncJobSchedulerProperties syncJobSchedulerProperties;

    @Test
    void createExecutorConfigBean_shouldReturnBeanFromSpringContext() {
        var context = new AnnotationConfigApplicationContext(SystemTimeConfig.class);
        assertNotNull(context.getAliases("sync-job-scheduler"));
    }

    @Test
    void shouldBindPropertiesFromEnvironment() {
        assertEquals(10, syncJobSchedulerProperties.getPollingTime());
    }

}