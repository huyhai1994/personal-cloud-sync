package org.mini_lab.personal_cloud_sync.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {

    private final ExecutorConfigProperties executorConfigProperties;


    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        int corePoolSize = executorConfigProperties.getCorePoolSize();
        int maximumPoolSize= executorConfigProperties.getMaximumPoolSize();
        int queueCapacity = executorConfigProperties.getQueueCapacity();
        long keepAliveTime = executorConfigProperties.getKeepAliveTime();

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity)
        );
    }
}
