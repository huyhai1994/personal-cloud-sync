package org.mini_lab.personal_cloud_sync.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class SystemTimeConfigTest {
    @Test
    void createSystemTimeBean_shouldReturnBeanFromSpringContext() {
        var context = new AnnotationConfigApplicationContext(SystemTimeConfig.class);
        assertNotNull(context.getAliases("systemClock"));
    }

}