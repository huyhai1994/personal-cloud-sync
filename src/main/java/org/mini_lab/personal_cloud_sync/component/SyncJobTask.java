package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.services.SyncJobProcessor;

public class SyncJobTask implements Runnable {
    private final Integer syncJobId;
    private final SyncJobProcessor syncJobProcessor;

    public SyncJobTask(Integer syncJobId, SyncJobProcessor syncJobProcessor) {
        this.syncJobId = syncJobId;
        this.syncJobProcessor = syncJobProcessor;
    }

    @Override
    public void run() {
        syncJobProcessor.process(syncJobId);
    }
}
