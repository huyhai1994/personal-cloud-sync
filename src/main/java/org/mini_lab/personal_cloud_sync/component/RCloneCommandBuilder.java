package org.mini_lab.personal_cloud_sync.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RCloneCommandBuilder {
    public List<String> command(String sourcePath, String targetPath) {
        return List.of("rclone", "sync", sourcePath, targetPath, "--log-level", "INFO");
    }

}
