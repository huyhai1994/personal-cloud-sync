package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.mini_lab.personal_cloud_sync.support.TestClockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestClockConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobStateManagerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    SyncJobStateManager syncJobStateManager;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncAttemptRepository syncAttemptRepository;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @AfterEach
    void tearDown() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    void updateHeartbeat_whenJobStatusNotRunning_thenHeartBeatIsNull() {
        SyncConfig syncConfig = saveSyncConfig("heart-beat-not-recorded");
        SyncJob syncJob = saveSyncJob(syncConfig, JobStatus.PENDING);
        Integer persistedSyncJobId = syncJob.getId();
        assertDoesNotThrow(() -> syncJobStateManager.updateHeartbeat(persistedSyncJobId));
        SyncJob updatedHeartBeatSyncConfig = syncJobRepository.getSyncJobById(persistedSyncJobId).orElseThrow();
        assertNull(updatedHeartBeatSyncConfig.getHeartBeatAt());
    }

    @Test
    void updateHeartbeat_whenClaimJobCountNotZero_thenHeartbeatRecorded() {
        SyncConfig syncConfig = saveSyncConfig("heart-beat-recorded");
        SyncJob syncJob = saveSyncJob(syncConfig, JobStatus.RUNNING);
        Integer persistedSyncJobId = syncJob.getId();
        assertDoesNotThrow(() -> syncJobStateManager.updateHeartbeat(persistedSyncJobId));
        SyncJob updatedHeartBeatSyncConfig = syncJobRepository.getSyncJobById(persistedSyncJobId).orElseThrow();
        assertNotNull(updatedHeartBeatSyncConfig.getHeartBeatAt());

    }

    private SyncConfig saveSyncConfig(String suffix) {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath.resolve(suffix).toString());
        syncConfig.setTargetPath(targetPath.resolve(suffix).toString());
        syncConfig.setScheduleType(ScheduleType.MANUAL);
        return syncConfigRepository.saveAndFlush(syncConfig);
    }

    private SyncJob saveSyncJob(SyncConfig syncConfig, JobStatus status) {
        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(status);
        return syncJobRepository.saveAndFlush(syncJob);
    }
}