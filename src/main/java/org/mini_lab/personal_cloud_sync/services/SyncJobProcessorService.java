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

    @Transactional
    public SyncJobContext markRunning(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING);
        assertOnlyOneJobClaimed(claimedJobCount);
        SyncJob syncJob = syncJobRepository.getSyncJobById(syncJobId).orElseThrow();
        SyncConfig syncConfig = syncJob.getSyncConfig();
        return new SyncJobContext(syncConfig.getSourcePath(), syncConfig.getTargetPath());
    }

    @Transactional
    public void markFailed(Integer syncJobId) {
        int claimJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED);
        assertOnlyOneJobClaimed(claimJobCount);
    }

    @Transactional
    public void markSuccess(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS);
        assertOnlyOneJobClaimed(claimedJobCount);
    }

    private void assertOnlyOneJobClaimed(int numberOfJobClaimed) {
        if (numberOfJobClaimed != 1) {
            throw new InvalidJobStateTransitionException();
        }
    }


}
