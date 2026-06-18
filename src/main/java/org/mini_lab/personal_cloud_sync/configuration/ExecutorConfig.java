package org.mini_lab.personal_cloud_sync.configuration;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {

    private final ExecutorConfigProperties executorConfigProperties;


    @Bean("syncJobExecutor")
    public ThreadPoolTaskExecutor syncJobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(executorConfigProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorConfigProperties.getMaximumPoolSize());
        executor.setQueueCapacity(executorConfigProperties.getQueueCapacity());
        executor.setKeepAliveSeconds((int) executorConfigProperties.getKeepAliveTime() / 1000);
        executor.setThreadNamePrefix("sync-job-");

        executor.setTaskDecorator(runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }

                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });

        executor.initialize();
        return executor;
    }
}
