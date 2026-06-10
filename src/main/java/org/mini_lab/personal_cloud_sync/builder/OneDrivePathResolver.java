package org.mini_lab.personal_cloud_sync.builder;

import org.springframework.stereotype.Component;

@Component
public class OneDrivePathResolver {
    public String normalizePath(String targetPath) {
        return "onedrive:" + targetPath;
    }
}
