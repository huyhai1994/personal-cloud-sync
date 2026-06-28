package org.mini_lab.personal_cloud_sync.support;

import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakeRCloneConfig {
    @Bean
    @Primary
    IRCloneExecutor rcloneExecutor(){
        return new FakeRCloneExecutor();
    }
}