package org.mini_lab.personal_cloud_sync.services;

import io.micrometer.core.annotation.Timed;
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

import java.time.Clock;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobProcessorService {
    private final SyncJobRepository syncJobRepository;
    private final SyncAttemptRecorder syncAttemptRecorder;
    private final Clock systemClock;

    @Transactional
    @Timed(
            value = "sync.job.processor.service.mark.running",
            description = "Time taken to change state from SUMMITED to RUNNING"
    )
    public SyncJobContext markRunning(Integer syncJobId) {
        log.info("MARK_RUNNING_STARTED syncJobId={}", syncJobId);

        int claimedJobCount = syncJobRepository.markRunningIfSubmitted(syncJobId, JobStatus.RUNNING, JobStatus.SUBMITTED, OffsetDateTime.now(systemClock));
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
    @Timed(
            value = "sync.job.processor.service.mark.failed",
            description = "Time taken to change state from RUNNING to FAILED"
    )
    public void markFailed(SyncJobContext syncJobContext, SyncErrorLog syncErrorLog) {
        Integer syncJobId = syncJobContext.syncJobId();
        int claimJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED);
        assertOnlyOneJobClaimed(claimJobCount);
        logStatusChange(JobStatus.RUNNING, JobStatus.FAILED);
        syncAttemptRecorder.markFailed(syncJobContext.syncAttemptId(), syncErrorLog);
    }

    @Transactional
    @Timed(
            value = "sync.job.processor.service.mark.success",
            description = "Time taken to change state from RUNNING to SUCCESS"
    )
    public void markSuccess(SyncJobContext syncJobContext) {
        Integer syncJobId = syncJobContext.syncJobId();
        Integer syncAttemptId = syncJobContext.syncAttemptId();
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS);
        assertOnlyOneJobClaimed(claimedJobCount);
        logStatusChange(JobStatus.RUNNING, JobStatus.SUCCESS);
        syncAttemptRecorder.markSuccess(syncAttemptId);
    }

    @Transactional
    @Timed(
            value = "sync.job.processor.service.mark.submit.failed",
            description = "Time taken to change state from PENDING to SUBMIT_FAILED"
    )
    public void markSubmitFailed(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.SUBMIT_FAILED);
        assertOnlyOneJobClaimed(claimedJobCount);
        logStatusChange(JobStatus.PENDING, JobStatus.SUBMIT_FAILED);
    }

    @Transactional
    @Timed(
            value = "sync.job.processor.service.mark.submitted",
            description = "Time taken to change state from PENDING to SUBMITTED"
    )
    public void markSubmitted(Integer syncJobId) {
        int claimedJobCount = syncJobRepository.markSubmittedIfPending(
                syncJobId,
                JobStatus.PENDING,
                JobStatus.SUBMITTED,
                OffsetDateTime.now(systemClock));
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
