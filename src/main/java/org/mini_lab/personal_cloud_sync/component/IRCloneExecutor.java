package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;

import java.io.IOException;

public interface IRCloneExecutor {
    RCloneResult sync(SyncJobContext syncJobContext) throws IOException, InterruptedException;
}
