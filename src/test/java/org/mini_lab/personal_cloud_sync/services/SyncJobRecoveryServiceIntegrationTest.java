package org.mini_lab.personal_cloud_sync.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobRecoveryServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SyncAttemptRepository syncAttemptRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncJobRecoveryService syncJobRecoveryService;

    @Autowired
    private EntityManager entityManager;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-11T00:00:00Z"),
            ZoneOffset.UTC
    );
    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    void recoverTimedOutRunningJobs_shouldMarkTimedOutRunningJobAndAttemptAsFailed() {
        SyncConfig syncConfig = saveSyncConfig("timed-out");
        SyncJob runningJob = saveSyncJob(syncConfig, JobStatus.RUNNING);
        SyncAttempt runningAttempt = saveSyncAttempt(runningJob, JobStatus.RUNNING);

        transactionTemplate.executeWithoutResult(status ->
                syncJobRepository.updateHeartBeatAtForTest(
                        runningJob.getId(),
                        OffsetDateTime.now().minusMinutes(30)
                )
        );

        syncJobRecoveryService.findTimedOutRunningJobs().forEach(id ->
                syncJobRecoveryService.updateTimedOutRunningJob(id)
        );

        SyncJob recoveredJob =
                syncJobRepository.findById(runningJob.getId()).orElseThrow();

        SyncAttempt recoveredAttempt =
                syncAttemptRepository.findById(runningAttempt.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(JobStatus.FAILED, recoveredJob.getFinalStatus()),
                () -> assertEquals(JobStatus.FAILED, recoveredAttempt.getAttemptStatus()),
                () -> assertEquals(SyncErrorCode.RECOVERY_TIMEOUT, recoveredAttempt.getErrorCode()), () -> assertNotNull(recoveredAttempt.getErrorMessage())
        );
    }

    @Test
    void recoverTimedOutRunningJobs_shouldNotRecoverRunningJob_whenJobIsNotTimedOut() {
        SyncConfig syncConfig = saveSyncConfig("not-timeout");
        SyncJob runningJob = saveSyncJob(syncConfig, JobStatus.RUNNING);
        SyncAttempt runningAttempt = saveSyncAttempt(runningJob, JobStatus.RUNNING);

        transactionTemplate.executeWithoutResult(status ->
                syncJobRepository.updateHeartBeatAtForTest(
                        runningJob.getId(),
                        OffsetDateTime.now().minusMinutes(5)
                )
        );

        syncJobRecoveryService.findTimedOutRunningJobs().forEach(id ->
                syncJobRecoveryService.updateTimedOutRunningJob(id)
        );

        SyncJob job = syncJobRepository.findById(runningJob.getId()).orElseThrow();
        SyncAttempt attempt = syncAttemptRepository.findById(runningAttempt.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(JobStatus.RUNNING, job.getFinalStatus()),
                () -> assertEquals(JobStatus.RUNNING, attempt.getAttemptStatus())
        );
    }

    @Test
    void recoverTimedOutRunningJobs_shouldNotTouchFinalJobs() {
        SyncConfig syncConfig = saveSyncConfig("final-status");

        SyncJob successJob = saveSyncJob(syncConfig, JobStatus.SUCCESS);

        SyncJob failedJob = saveSyncJob(syncConfig, JobStatus.FAILED);

        transactionTemplate.executeWithoutResult(status -> {
            syncJobRepository.updateHeartBeatAtForTest(
                    successJob.getId(),
                    OffsetDateTime.now().minusMinutes(30)
            );
            syncJobRepository.updateHeartBeatAtForTest(
                    failedJob.getId(),
                    OffsetDateTime.now().minusMinutes(30)
            );
        });

        syncJobRecoveryService.findTimedOutRunningJobs().forEach(id ->
                syncJobRecoveryService.updateTimedOutRunningJob(id)
        );

        SyncJob foundSuccessJob = syncJobRepository.findById(successJob.getId()).orElseThrow();
        SyncJob foundFailedJob = syncJobRepository.findById(failedJob.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(JobStatus.SUCCESS, foundSuccessJob.getFinalStatus()),
                () -> assertEquals(JobStatus.FAILED, foundFailedJob.getFinalStatus())
        );
    }


    private SyncConfig saveSyncConfig(String suffix) {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath.resolve(suffix).toString());
        syncConfig.setTargetPath(targetPath.resolve(suffix).toString());
        return syncConfigRepository.saveAndFlush(syncConfig);
    }

    private SyncJob saveSyncJob(SyncConfig syncConfig, JobStatus status) {
        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(status);
        return syncJobRepository.saveAndFlush(syncJob);
    }

    private SyncAttempt saveSyncAttempt(SyncJob syncJob, JobStatus status) {
        SyncAttempt syncAttempt = new SyncAttempt();
        syncAttempt.setSyncJob(syncJob);
        syncAttempt.setAttemptStatus(status);
        syncAttempt.setStartAt(OffsetDateTime.now(fixedClock));
        return syncAttemptRepository.saveAndFlush(syncAttempt);
    }
}