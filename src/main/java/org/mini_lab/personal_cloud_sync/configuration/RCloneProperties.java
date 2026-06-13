package org.mini_lab.personal_cloud_sync.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rclone")
@Getter
@Setter
public class RCloneProperties {
    private int timeOutSecond;
}
