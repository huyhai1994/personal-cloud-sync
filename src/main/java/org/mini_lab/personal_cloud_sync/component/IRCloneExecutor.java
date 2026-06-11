package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.dto.RCloneResult;

import java.io.IOException;

public interface IRCloneExecutor {
    RCloneResult sync(String sourcePath, String targetPath) throws IOException, InterruptedException;
}
