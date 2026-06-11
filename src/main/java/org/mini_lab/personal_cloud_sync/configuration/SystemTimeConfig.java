package org.mini_lab.personal_cloud_sync.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SystemTimeConfig {
    @Bean(name = "systemClock")
    public Clock clock() {
        return Clock.systemUTC();
    }
}
