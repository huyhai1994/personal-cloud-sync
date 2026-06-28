package org.mini_lab.personal_cloud_sync.support;

import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;

import java.io.IOException;

public class FakeRCloneExecutor implements IRCloneExecutor {

    @Override
    public RCloneResult sync(SyncJobContext syncJobContext)
            throws IOException, InterruptedException {

        RCloneResult result = new RCloneResult();
        result.setExitCode(0);
        return result;
    }
}