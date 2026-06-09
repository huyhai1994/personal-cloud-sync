package org.mini_lab.personal_cloud_sync.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobRepositoryTest {

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @BeforeEach
    void setUp() {
        syncJobRepository.deleteAll();
    }

    @Test
    void saveSyncJob_shouldSuccess() {
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


}