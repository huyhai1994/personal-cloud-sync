package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Service
@RequiredArgsConstructor
public class SyncJobDispatcher {

    @Qualifier("syncJobExecutor")
    private final ExecutorService syncJobExecutor;

    private final SyncJobProcessor syncJobProcessor;
    private final SyncJobProcessorService syncJobProcessorService;

    public void dispatch(Integer syncJobId) {
        try {
            syncJobExecutor.submit(() -> syncJobProcessor.process(syncJobId));
            syncJobProcessorService.markSubmitted(syncJobId);
        } catch (RejectedExecutionException e) {
            syncJobProcessorService.markSubmitFailed(syncJobId);
        }
    }
}