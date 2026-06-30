package org.mini_lab.personal_cloud_sync.services;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobProcessor {
    private final IRCloneExecutor rCloneExecutor;
    private final SyncJobStateManager syncJobStateManager;
    private final SyncConfigValidator syncConfigValidator;
    private final ScheduledThreadPoolExecutor heartbeatExecutor;

    @Timed(
            value = "sync.job.processor.process",
            description = "Time taken to process an Job"
    )
    public void process(Integer syncJobId) {
        log.info("SYNC_JOB_PROCESS_STARTED");
        SyncJobContext syncJobContext = syncJobStateManager.markRunning(syncJobId);
        ScheduledFuture<?> heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        syncJobStateManager.updateHeartbeat(syncJobId);
                    } catch (Exception e) {
                        log.warn("UPDATE_HEARTBEAT_FAILED syncJobId={}", syncJobId, e);
                    }
                },
                5,
                5,
                TimeUnit.SECONDS
        );
        try {
            validate(syncJobContext);
            RCloneResult rCloneResult = rCloneExecutor.sync(syncJobContext);
            log.info("RCLONE_FINISHED exitCode={} errorMessage={}",
                    rCloneResult.getExitCode(), rCloneResult.getErrorMessage());
            if (rCloneResult.isSuccess()) {
                syncJobStateManager.markSuccess(syncJobContext);
            } else {
                SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.SYNC_PROCESS_ERROR, "Rclone process finished with non-zero exit code");
                syncJobStateManager.markFailed(syncJobContext, syncErrorLog);
            }
        } catch (IOException e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.IO_ERROR, "IOException occurred while starting process or reading process output");
            syncJobStateManager.markFailed(syncJobContext, syncErrorLog);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.INTERRUPTED, "Worker thread was interrupted while waiting for sync process");
            syncJobStateManager.markFailed(syncJobContext, syncErrorLog);
        } catch (InvalidPathException | LocalPathIsNotDirectory e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.VALIDATION_ERROR, "Source path / target path invalid before running sync job");
            syncJobStateManager.markFailed(syncJobContext, syncErrorLog);
        } catch (Exception e) {
            SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.UNKNOWN_ERROR, e.getMessage());
            syncJobStateManager.markFailed(syncJobContext, syncErrorLog);
        } finally {
            heartbeatTask.cancel(true);
        }
    }

    private void validate(SyncJobContext syncJobContext) {
        String sourcePath = syncJobContext.sourcePath();
        String targetPath = syncJobContext.targetPath();
        syncConfigValidator.validateSourcePath(sourcePath);
        syncConfigValidator.validateTargetPath(targetPath);
    }
}
