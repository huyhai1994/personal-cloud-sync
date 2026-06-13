package org.mini_lab.personal_cloud_sync.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobRepositoryTest {

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void saveSyncJob_missing_finalStatus_shouldThrow() {
        SyncJob syncJob = new SyncJob();
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(syncConfig);

        syncJob.setSyncConfig(persistedSyncConfig);
        assertThrows(DataIntegrityViolationException.class, () -> {
            syncJobRepository.saveAndFlush(syncJob);
        });
    }

    @Test
    void saveSyncJob_should_success() {
        SyncJob syncJob = new SyncJob();
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(syncConfig);

        syncJob.setSyncConfig(persistedSyncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob persistedSyncJob = syncJobRepository.saveAndFlush(syncJob);
        assertNotNull(persistedSyncConfig.getCreatedAt());
        assertNotNull(persistedSyncJob.getId());
    }

    @Test
    void getAllSyncJob_pendingStatus() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig =
                syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob firstSyncJob = new SyncJob();
        firstSyncJob.setSyncConfig(persistedSyncConfig);
        firstSyncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob secondSyncJob = new SyncJob();
        secondSyncJob.setSyncConfig(persistedSyncConfig);
        secondSyncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob notPendingSyncJob = new SyncJob();
        notPendingSyncJob.setSyncConfig(persistedSyncConfig);
        notPendingSyncJob.setFinalStatus(JobStatus.RUNNING);
        syncJobRepository.saveAndFlush(firstSyncJob);
        syncJobRepository.saveAndFlush(secondSyncJob);
        syncJobRepository.saveAndFlush(notPendingSyncJob);

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
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig =
                syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob initialSyncJob = new SyncJob();
        initialSyncJob.setSyncConfig(persistedSyncConfig);
        initialSyncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob syncJob = syncJobRepository.saveAndFlush(initialSyncJob);
        Integer syncJobId = syncJob.getId();
        Optional<SyncJob> foundSyncJobOpt = syncJobRepository.getSyncJobById(syncJobId);
        assertNotNull(foundSyncJobOpt.orElseThrow());
    }

    @Test
    @DisplayName("Business Rule: check for duplicate sync job before create a new one")
    void existBySyncConfigAndStatus_shouldReturnTrue_whenPendingJobExists() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig =
                syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob firstSyncJob = new SyncJob();
        firstSyncJob.setSyncConfig(persistedSyncConfig);
        firstSyncJob.setFinalStatus(JobStatus.PENDING);

        syncJobRepository.saveAndFlush(firstSyncJob);
        assertTrue(syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(persistedSyncConfig.getId(), List.of(JobStatus.PENDING, JobStatus.RUNNING)));
    }

    @Test
    void getSyncJobById_shouldFetchSynConfigImmediately() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(persistedSyncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);
        SyncJob savedSyncJob = syncJobRepository.saveAndFlush(syncJob);

        Integer id = savedSyncJob.getId();

        entityManager.flush();
        entityManager.clear();

        SyncJob foundSyncJob = syncJobRepository.getSyncJobById(id).orElseThrow();

        assertNotNull(foundSyncJob.getSyncConfig());

        assertTrue(
                entityManagerFactory.getPersistenceUnitUtil()
                        .isLoaded(foundSyncJob, "syncConfig")
        );

        assertEquals("/source/test", foundSyncJob.getSyncConfig().getSourcePath());
    }
}