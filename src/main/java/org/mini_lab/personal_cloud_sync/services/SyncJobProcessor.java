package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SyncJobProcessor {
    private final IRCloneExecutor rCloneExecutor;
    private final SyncJobStatusService syncJobProcessorService;

    public void process(Integer syncJobId) {
        SyncJobContext syncJobContext = syncJobProcessorService.markRunning(syncJobId);
        try {
            RCloneResult rCloneResult = rCloneExecutor.sync(syncJobContext.sourcePath(), syncJobContext.targetPath());
            if (rCloneResult.isSuccess()) {
                syncJobProcessorService.markSuccess(syncJobId);
            } else {
                syncJobProcessorService.markFailed(syncJobId);
            }
        } catch (IOException e) {
            syncJobProcessorService.markFailed(syncJobId);
        } catch (InterruptedException e) {
            syncJobProcessorService.markFailed(syncJobId);
            Thread.currentThread().interrupt();
        }
    }
}
