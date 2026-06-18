package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobDispatcher {

    @Qualifier("syncJobExecutor")
    private final TaskExecutor syncJobExecutor;

    private final SyncJobProcessor syncJobProcessor;
    private final SyncJobProcessorService syncJobProcessorService;

    public void dispatch(Integer syncJobId) {
        try {
            log.info("SYNC_JOB_DISPATCHED syncJobId={}", syncJobId);
            syncJobProcessorService.markSubmitted(syncJobId);
            syncJobExecutor.execute(() -> {
                MDC.put("syncJobId", syncJobId.toString());
                syncJobProcessor.process(syncJobId);
            });
        } catch (RejectedExecutionException e) {
            syncJobProcessorService.markSubmitFailed(syncJobId);
        }
    }
}