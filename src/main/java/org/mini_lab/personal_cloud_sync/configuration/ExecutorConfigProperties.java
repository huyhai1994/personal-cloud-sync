package org.mini_lab.personal_cloud_sync.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "executor-config")
@Getter
@Setter
public class ExecutorConfigProperties {
    private int corePoolSize;
    private int maximumPoolSize;
    private int queueCapacity;
    private long keepAliveTime;
    private int heartBeatCorePoolSize;
}
