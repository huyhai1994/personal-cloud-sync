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
class ExecutorConfigPropertiesTest extends AbstractIntegrationTest {

    @Autowired
    ExecutorConfigProperties executorConfigProperties;

    @Test
    void createExecutorConfigBean_shouldReturnBeanFromSpringContext() {
        var context = new AnnotationConfigApplicationContext(ExecutorConfigProperties.class);
        assertNotNull(context.getAliases("executor-config"));
    }

    @Test
    void shouldBindPropertiesFromEnvironment() {
        assertEquals(5, executorConfigProperties.getCorePoolSize());
        assertEquals(100, executorConfigProperties.getQueueCapacity());
        assertEquals(0L, executorConfigProperties.getKeepAliveTime());
    }

}