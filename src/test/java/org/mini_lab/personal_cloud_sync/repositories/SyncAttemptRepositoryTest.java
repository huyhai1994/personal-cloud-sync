package org.mini_lab.personal_cloud_sync.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncAttemptRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    SyncAttemptRepository syncAttemptRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private EntityManager entityManager;

    private final Clock startedAt = Clock.fixed(
            Instant.parse("2026-06-05T10:00:00Z"),
            ZoneOffset.UTC
    );

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @Test
    void saveInitialSyncAttempt_shouldSuccess() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath.toString());
        syncConfig.setTargetPath(targetPath.toString());
        SyncConfig persistedSyncConfig =
                syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(persistedSyncConfig);
        syncJob.setFinalStatus(JobStatus.RUNNING);
        syncJobRepository.saveAndFlush(syncJob);
        assertTrue(syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(persistedSyncConfig.getId(), List.of(JobStatus.PENDING, JobStatus.RUNNING)));
        SyncAttempt syncAttempt = new SyncAttempt();
        syncAttempt.setAttemptStatus(JobStatus.RUNNING);
        syncAttempt.setStartAt(OffsetDateTime.now(startedAt));
        syncAttempt.setSyncJob(syncJob);
        Integer syncAttemptId = syncAttemptRepository.saveAndFlush(syncAttempt).getId();
        assertNotNull(syncAttemptId);

        entityManager.flush();
        entityManager.clear();
        Integer savedSyncJobId = syncAttemptRepository.findById(syncAttemptId).orElseThrow().getId();
        assertEquals(syncAttemptId, savedSyncJobId);
    }


}