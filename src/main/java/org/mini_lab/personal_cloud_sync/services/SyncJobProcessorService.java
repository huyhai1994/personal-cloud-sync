package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncJobProcessorService {
    private final SyncJobRepository syncJobRepository;
    private final SyncAttemptRecorder syncAttemptRecorder;

    @Transactional
    public SyncJobContext markRunning(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING);
        assertOnlyOneJobClaimed(claimedJobCount);
        SyncJob syncJob = syncJobRepository.getSyncJobById(syncJobId).orElseThrow();
        SyncConfig syncConfig = syncJob.getSyncConfig();
        Integer syncAttemptId = syncAttemptRecorder.startAttempt(syncJob);
        return new SyncJobContext(syncJob.getId(), syncAttemptId, syncConfig.getSourcePath(), syncConfig.getTargetPath());
    }

    @Transactional
    public void markFailed(SyncJobContext syncJobContext) {
        Integer syncJobId = syncJobContext.syncJobId();
        int claimJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED);
        assertOnlyOneJobClaimed(claimJobCount);
    }

    @Transactional
    public void markSuccess(SyncJobContext syncJobContext) {
        Integer syncJobId = syncJobContext.syncJobId();
        Integer syncAttemptId = syncJobContext.syncAttemptId();
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS);
        assertOnlyOneJobClaimed(claimedJobCount);
        syncAttemptRecorder.markSuccess(syncAttemptId);
    }

    private void assertOnlyOneJobClaimed(int numberOfJobClaimed) {
        if (numberOfJobClaimed != 1) {
            throw new InvalidJobStateTransitionException();
        }
    }


}
