package org.mini_lab.personal_cloud_sync.services;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.configuration.RecoverySchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SyncJobRecoveryService {
    private final SyncJobRepository syncJobRepository;

    private final Clock systemClock;

    private final RecoverySchedulerProperties recoverySchedulerProperties;

    @Transactional
    @Timed("sync.job.recovery.service.update.timed.out.running.job")
    public void updateTimedOutRunningJob(Integer jobId) {
        OffsetDateTime markFailedTime = OffsetDateTime.now(systemClock);

        int updatedRows = syncJobRepository.markFailedIfRunning(
                jobId,
                JobStatus.FAILED,
                JobStatus.RUNNING,
                markFailedTime
        );

        if (updatedRows == 0) {
            log.info("SKIP_RECOVER_STUCK_RUNNING_JOB syncJobId={} reason=status_changed", jobId);
            return;
        }

        SyncJob job = syncJobRepository.getSyncJobById(jobId).orElseThrow();

        log.info("RECOVER_STUCK_RUNNING_JOB syncJobId={}", job.getId());

        job.getSyncAttempts().forEach(attempt -> {
            attempt.setAttemptStatus(JobStatus.FAILED);
            attempt.setErrorCode(SyncErrorCode.RECOVERY_TIMEOUT);
            attempt.setErrorMessage("Stuck running job was marked as FAILED by recovery scheduler");
            attempt.setFinishedAt(markFailedTime);
        });
    }

    @Transactional(readOnly = true)
    public List<Integer> findTimedOutRunningJobs() {
        return syncJobRepository.findTimedOutRunningJobs(
                JobStatus.RUNNING,
                OffsetDateTime.now(systemClock).minusMinutes(recoverySchedulerProperties.getRunningJobTimedOutLimit()),
                PageRequest.of(0, 10,
                        Sort.by("heartBeatAt")
                                .ascending())
        );
    }
}
