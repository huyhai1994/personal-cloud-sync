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
    private final SyncJobProcessorService syncJobProcessorService;

    public void process(Integer syncJobId) {
        SyncJobContext syncJobContext = syncJobProcessorService.markRunning(syncJobId);
        try {
            RCloneResult rCloneResult = rCloneExecutor.sync(syncJobContext);
            if (rCloneResult.isSuccess()) {
                syncJobProcessorService.markSuccess(syncJobContext);
            } else {
                syncJobProcessorService.markFailed(syncJobContext);
            }
        } catch (IOException e) {
            syncJobProcessorService.markFailed(syncJobContext);
        } catch (InterruptedException e) {
            syncJobProcessorService.markFailed(syncJobContext);
            Thread.currentThread().interrupt();
        }
    }
}
