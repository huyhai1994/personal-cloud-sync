package org.mini_lab.personal_cloud_sync.services;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.configuration.RecoverySchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
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
    @Timed("")
    public void findAndUpdateTimedOutRunningJobs() {
        List<SyncJob> timedOutRunningJobs =
                syncJobRepository.findTimedOutRunningJobs(
                        JobStatus.RUNNING,
                        OffsetDateTime.now(systemClock).minusMinutes(recoverySchedulerProperties.getRunningJobTimedOutLimit())
                );

        timedOutRunningJobs.forEach(job -> {
            job.setFinalStatus(JobStatus.FAILED);
            log.info("RECOVER_STUCK_RUNNING_JOB syncJobId={}",job.getId());
            job.getSyncAttempts().forEach(attempt -> {
                attempt.setAttemptStatus(JobStatus.FAILED);
                attempt.setErrorCode(SyncErrorCode.RECOVERY_TIMEOUT);
                attempt.setErrorMessage("Stuck running job was marked as FAILED by recovery scheduler");
                attempt.setFinishedAt(OffsetDateTime.now(systemClock));
            });
        });
    }
}
