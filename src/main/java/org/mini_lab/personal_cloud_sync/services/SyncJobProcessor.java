package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
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
                SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.SYNC_PROCESS_ERROR, "Rclone process finished with non-zero exit code");
                syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
            }
        } catch (IOException e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.IO_ERROR, "IOException occurred while starting process or reading process output");
            syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.INTERRUPTED, "Worker thread was interrupted while waiting for sync process");
            syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
        } catch (Exception e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.UNKNOWN_ERROR, e.getMessage());
            syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
        }
    }
}
