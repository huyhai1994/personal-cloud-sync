package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobProcessorService {
    private final SyncJobRepository syncJobRepository;
    private final SyncAttemptRecorder syncAttemptRecorder;

    @Transactional
    public SyncJobContext markRunning(Integer syncJobId) {
        log.info("MARK_RUNNING_STARTED syncJobId={}", syncJobId);

        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.SUBMITTED, JobStatus.RUNNING);
        log.info("MARK_RUNNING_UPDATE_DONE syncJobId={} claimedJobCount={}",
                syncJobId, claimedJobCount);

        assertOnlyOneJobClaimed(claimedJobCount);
        log.info("MARK_RUNNING_ASSERT_DONE syncJobId={}", syncJobId);

        logStatusChange(JobStatus.SUBMITTED, JobStatus.RUNNING);

        SyncJob syncJob = syncJobRepository.getSyncJobById(syncJobId).orElseThrow();
        log.info("MARK_RUNNING_LOAD_JOB_DONE syncJobId={}", syncJobId);

        SyncConfig syncConfig = syncJob.getSyncConfig();
        Integer syncAttemptId = syncAttemptRecorder.startAttempt(syncJob);
        return new SyncJobContext(syncJob.getId(), syncAttemptId, syncConfig.getSourcePath(), syncConfig.getTargetPath());
    }

    @Transactional
    public void markFailed(SyncJobContext syncJobContext, SyncErrorLog syncErrorLog) {
        Integer syncJobId = syncJobContext.syncJobId();
        int claimJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED);
        assertOnlyOneJobClaimed(claimJobCount);
        logStatusChange(JobStatus.RUNNING, JobStatus.FAILED);
        syncAttemptRecorder.markFailed(syncJobContext.syncAttemptId(), syncErrorLog);
    }

    @Transactional
    public void markSuccess(SyncJobContext syncJobContext) {
        Integer syncJobId = syncJobContext.syncJobId();
        Integer syncAttemptId = syncJobContext.syncAttemptId();
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS);
        assertOnlyOneJobClaimed(claimedJobCount);
        logStatusChange(JobStatus.RUNNING, JobStatus.SUCCESS);
        syncAttemptRecorder.markSuccess(syncAttemptId);
    }

    @Transactional
    public void markSubmitFailed(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.SUBMIT_FAILED);
        assertOnlyOneJobClaimed(claimedJobCount);
        logStatusChange(JobStatus.PENDING, JobStatus.SUBMIT_FAILED);
    }

    @Transactional
    public void markSubmitted(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.SUBMITTED);
        assertOnlyOneJobClaimed(claimedJobCount);
        logStatusChange(JobStatus.PENDING, JobStatus.SUBMITTED);
    }

    private static void logStatusChange(JobStatus from, JobStatus to) {
        log.info("SYNC_JOB_STATUS_CHANGED  from={} to={}",
                from, to);
    }

    private void assertOnlyOneJobClaimed(int numberOfJobClaimed) {
        if (numberOfJobClaimed != 1) {
            throw new InvalidJobStateTransitionException();
        }
    }

}
