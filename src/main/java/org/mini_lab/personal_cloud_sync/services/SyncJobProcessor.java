package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.component.SyncConfigValidator;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.InvalidPathException;

@Service
@RequiredArgsConstructor
public class SyncJobProcessor {
    private final IRCloneExecutor rCloneExecutor;
    private final SyncJobProcessorService syncJobProcessorService;
    private final SyncConfigValidator syncConfigValidator;

    public void process(Integer syncJobId) {
        SyncJobContext syncJobContext = syncJobProcessorService.markRunning(syncJobId);
        try {
            validate(syncJobContext);
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
        } catch (InvalidPathException | LocalPathIsNotDirectory e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.VALIDATION_ERROR, "Source path / target path invalid before running sync job");
            syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
        } catch (Exception e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.UNKNOWN_ERROR, e.getMessage());
            syncJobProcessorService.markFailed(syncJobContext, syncErrorLog);
        }
    }

    private void validate(SyncJobContext syncJobContext) {
        String sourcePath = syncJobContext.sourcePath();
        String targetPath = syncJobContext.targetPath();
        syncConfigValidator.validateSourcePath(sourcePath);
        syncConfigValidator.validateTargetPath(targetPath);
    }
}
