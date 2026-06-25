package org.mini_lab.personal_cloud_sync.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    private final Clock currentTime = Clock.fixed(
            Instant.parse("2026-06-23T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void saveSyncJob_missingFinalStatus_shouldThrow() {
        SyncConfig persistedSyncConfig = saveSyncConfig("missing-final-status");

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(persistedSyncConfig);

        assertThrows(DataIntegrityViolationException.class, () ->
                syncJobRepository.saveAndFlush(syncJob)
        );
    }

    @Test
    void saveSyncJob_shouldSuccess() {
        SyncConfig persistedSyncConfig = saveSyncConfig("save-success");

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(persistedSyncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);

        assertNotNull(persistedSyncConfig.getCreatedAt());
        assertNotNull(persistedSyncJob.getId());
    }

    @Test
    void getAllSyncJob_shouldReturnOnlyPendingJobs_whenPendingStatusRequested() {
        SyncConfig persistedSyncConfig = saveSyncConfig("pending-status");

        SyncJob firstPendingJob = buildSyncJob(persistedSyncConfig, JobStatus.PENDING);
        SyncJob secondPendingJob = buildSyncJob(persistedSyncConfig, JobStatus.PENDING);
        SyncJob runningJob = buildSyncJob(persistedSyncConfig, JobStatus.RUNNING);

        syncJobRepository.saveAndFlush(firstPendingJob);
        syncJobRepository.saveAndFlush(secondPendingJob);
        syncJobRepository.saveAndFlush(runningJob);

        List<SyncJob> result =
                syncJobRepository.getAllByFinalStatus(
                        JobStatus.PENDING,
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.size());
        assertTrue(
                result.stream()
                        .allMatch(job -> job.getFinalStatus() == JobStatus.PENDING)
        );
    }

    @Test
    void getSyncJobById_shouldReturnOptionalSyncJob() {
        SyncConfig persistedSyncConfig = saveSyncConfig("find-by-id");

        SyncJob initialSyncJob = buildSyncJob(persistedSyncConfig, JobStatus.PENDING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(initialSyncJob);

        Optional<SyncJob> foundSyncJobOpt =
                syncJobRepository.getSyncJobById(persistedSyncJob.getId());

        assertTrue(foundSyncJobOpt.isPresent());
    }

    @Test
    @DisplayName("Business Rule: check for duplicate sync job before creating a new one")
    void existsBySyncConfigIdAndFinalStatusIn_shouldReturnTrue_whenPendingJobExists() {
        SyncConfig persistedSyncConfig = saveSyncConfig("duplicate-check");

        SyncJob pendingJob = buildSyncJob(persistedSyncConfig, JobStatus.PENDING);
        syncJobRepository.saveAndFlush(pendingJob);

        assertTrue(
                syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(
                        persistedSyncConfig.getId(),
                        List.of(JobStatus.PENDING, JobStatus.RUNNING)
                )
        );
    }

    @Test
    void getSyncJobById_shouldFetchSyncConfigImmediately() {
        SyncConfig persistedSyncConfig = saveSyncConfig("fetch-sync-config");

        SyncJob syncJob = buildSyncJob(persistedSyncConfig, JobStatus.PENDING);
        SyncJob savedSyncJob = syncJobRepository.saveAndFlush(syncJob);

        entityManager.flush();
        entityManager.clear();

        SyncJob foundSyncJob =
                syncJobRepository.getSyncJobById(savedSyncJob.getId())
                        .orElseThrow();

        assertNotNull(foundSyncJob.getSyncConfig());

        assertTrue(
                entityManagerFactory.getPersistenceUnitUtil()
                        .isLoaded(foundSyncJob, "syncConfig")
        );

        assertEquals(
                sourcePath.resolve("fetch-sync-config").toString(),
                foundSyncJob.getSyncConfig().getSourcePath()
        );
    }

    @Test
    void findTimedOutRunningJobs_shouldReturnTimedOutJobs() {
        SyncConfig timoutPersistedSyncConfig = saveSyncConfig("timed-out-running-job");
        SyncConfig notTimeOutPersistedSyncConfig = saveSyncConfig("not-timed-out-running-job");

        SyncJob timeoutSyncJob = buildSyncJob(timoutPersistedSyncConfig, JobStatus.RUNNING);
        timeoutSyncJob.setHeartBeatAt(OffsetDateTime.now(currentTime).minusMinutes(20));
        syncJobRepository.saveAndFlush(timeoutSyncJob);

        SyncJob notTimeoutSyncJob = buildSyncJob(notTimeOutPersistedSyncConfig, JobStatus.RUNNING);
        notTimeoutSyncJob.setHeartBeatAt(OffsetDateTime.now(currentTime).minusMinutes(5));
        syncJobRepository.saveAndFlush(notTimeoutSyncJob);

        entityManager.flush();
        entityManager.clear();

        List<SyncJob> timedOutRunningJobs =
                syncJobRepository.findTimedOutRunningJobs(
                        JobStatus.RUNNING,
                        OffsetDateTime.now(currentTime).minusMinutes(15L)
                );

        assertEquals(1, timedOutRunningJobs.size());
    }

    @Test
    void markSubmittedIfPending_shouldUpdateNewStatusAndRecordSubmittedAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("submitted-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.PENDING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.markSubmittedIfPending(
                id,
                JobStatus.PENDING,
                JobStatus.SUBMITTED,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getSubmittedAt());
        assertEquals(JobStatus.SUBMITTED, updatedSyncJob.getFinalStatus());
    }

    @Test
    void markSubmitFailedIfPending_shouldUpdateNewStatusAndRecordSubmitFailedAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("submit-failed-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.PENDING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.markSubmittedIfPending(
                id,
                JobStatus.PENDING,
                JobStatus.SUBMIT_FAILED,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getSubmittedAt());
        assertEquals(JobStatus.SUBMIT_FAILED, updatedSyncJob.getFinalStatus());
    }

    @Test
    void markRunningIfSubmitted_shouldUpdateNewStatusAndRecordStartAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("start-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.SUBMITTED);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.markRunningIfSubmitted(
                id,
                JobStatus.RUNNING,
                JobStatus.SUBMITTED,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getStartAt());
        assertEquals(JobStatus.RUNNING, updatedSyncJob.getFinalStatus());
    }

    @Test
    void updateHeartBeatIfRunning_shouldUpdateHeartBeatAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("heartbeat-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.RUNNING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.updateHeartbeatIfRunning(
                id,
                JobStatus.RUNNING,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getHeartBeatAt());
        assertEquals(JobStatus.RUNNING, updatedSyncJob.getFinalStatus());
    }

    @Test
    void markSuccessIfRunning_shouldUpdateNewStatusAndRecordFinishedAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("finished-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.RUNNING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.markSuccessIfRunning(
                id,
                JobStatus.SUCCESS,
                JobStatus.RUNNING,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getFinishedAt());
        assertEquals(JobStatus.SUCCESS, updatedSyncJob.getFinalStatus());
    }

    @Test
    void markFailedIfRunning_shouldUpdateNewStatusAndRecordFinishedAt() {
        // given
        SyncConfig syncConfig = saveSyncConfig("finished-at");
        SyncJob syncJob = buildSyncJob(syncConfig, JobStatus.RUNNING);
        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        Integer id = persistedSyncJob.getId();

        entityManager.clear();

        // when
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        int updatedCount = syncJobRepository.markFailedIfRunning(
                id,
                JobStatus.FAILED,
                JobStatus.RUNNING,
                now
        );

        entityManager.flush();
        entityManager.clear();

        // then
        SyncJob updatedSyncJob = syncJobRepository.findById(id).orElseThrow();

        assertEquals(1, updatedCount);
        assertEquals(now, updatedSyncJob.getFinishedAt());
        assertEquals(JobStatus.FAILED, updatedSyncJob.getFinalStatus());
    }

    private SyncConfig saveSyncConfig(String pathSuffix) {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath.resolve(pathSuffix).toString());
        syncConfig.setTargetPath(targetPath.resolve(pathSuffix).toString());
        syncConfig.setScheduleType(ScheduleType.MANUAL);
        return syncConfigRepository.saveAndFlush(syncConfig);
    }

    private SyncJob buildSyncJob(SyncConfig syncConfig, JobStatus finalStatus) {
        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(finalStatus);
        return syncJob;
    }
}