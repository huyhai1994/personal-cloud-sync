package org.mini_lab.personal_cloud_sync.builder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OneDriveCommandBuilder {
    public List<String> command(String sourcePath, String targetPath) {
        targetPath = normalizePath(targetPath);
        return List.of("rclone", "sync", sourcePath, "onedrive:" + targetPath, "--verbose", "--log-level", "INFO");
    }

    private static String normalizePath(String targetPath) {
        if (targetPath.startsWith("/")) {
            targetPath = targetPath.substring(1);
        }
        return targetPath;
    }

}
