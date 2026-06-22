package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncAttemptRecorderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SyncAttemptRepository syncAttemptRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncAttemptRecorder syncAttemptRecorder;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private SyncConfig persistedSyncConfig;
    private SyncJob persistedSyncJob;

    @BeforeEach
    void setUp() {
        persistedSyncConfig = transactionTemplate.execute((status -> {
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setSourcePath("/source/test");
            syncConfig.setTargetPath("/target/test");
            return syncConfigRepository.save(syncConfig);
        }));
        persistedSyncJob = transactionTemplate.execute(status -> {
            SyncJob syncJob = new SyncJob();
            syncJob.setSyncConfig(persistedSyncConfig);
            syncJob.setFinalStatus(JobStatus.RUNNING);
            return syncJobRepository.save(syncJob);
        });
        assertNotNull(persistedSyncConfig);
        assertTrue(syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(persistedSyncConfig.getId(), List.of(JobStatus.PENDING, JobStatus.RUNNING)));

    }

    @AfterEach
    void tearDown() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    void startAttempt_whenSuccess_shouldCreateRunningAttempt() {
        Integer syncAttemptId = transactionTemplate.execute(status -> syncAttemptRecorder.startAttempt(persistedSyncJob));
        assertNotNull(syncAttemptId);
        SyncAttempt persistedSyncAttempt = transactionTemplate.execute(status -> syncAttemptRepository.findById(syncAttemptId).orElseThrow());
        assertNotNull(persistedSyncAttempt);
        assertNull(persistedSyncAttempt.getFinishedAt());
        assertNotNull(persistedSyncAttempt.getStartAt());
        assertEquals(JobStatus.RUNNING, persistedSyncAttempt.getAttemptStatus());

    }

    @Test
    void markSuccess_whenNoTransaction_shouldThrowIllegalTransactionStateException() {

        Integer syncAttemptId = transactionTemplate.execute(status -> syncAttemptRecorder.startAttempt(persistedSyncJob));
        assertNotNull(syncAttemptId);
        SyncAttempt persistedSyncAttempt = transactionTemplate.execute(status -> syncAttemptRepository.findById(syncAttemptId).orElseThrow());
        assertNotNull(persistedSyncAttempt.getId());
        assertThrows(IllegalTransactionStateException.class, () -> syncAttemptRecorder.markSuccess(persistedSyncAttempt.getId()));
    }

    @Test
    void markSuccess_whenRunningAttempt_shouldUpdateToSuccessAndFinishAt() {
        Integer syncAttemptId = transactionTemplate.execute(status -> syncAttemptRecorder.startAttempt(persistedSyncJob));
        assertNotNull(syncAttemptId);
        SyncAttempt persistedSyncAttempt = transactionTemplate.execute(status -> syncAttemptRepository.findById(syncAttemptId).orElseThrow());
        assertNotNull(persistedSyncAttempt.getId());
        transactionTemplate.executeWithoutResult(status -> syncAttemptRecorder.markSuccess(persistedSyncAttempt.getId()));
        SyncAttempt saved = transactionTemplate.execute(status -> syncAttemptRepository.findById(persistedSyncAttempt.getId())).get();
        assertEquals(JobStatus.SUCCESS, saved.getAttemptStatus());
        assertNotNull(saved.getFinishedAt());
    }

    @Test
    void markFailed_whenRunningAttempt_shouldUpdateToFailedAndFinishAt() {
        Integer syncAttemptId = transactionTemplate.execute(status -> syncAttemptRecorder.startAttempt(persistedSyncJob));
        assertNotNull(syncAttemptId);
        SyncAttempt persistedSyncAttempt = transactionTemplate.execute(status -> syncAttemptRepository.findById(syncAttemptId).orElseThrow());
        assertNotNull(persistedSyncAttempt.getId());
        SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.SYNC_PROCESS_ERROR, "Rclone process finished with non-zero exit code");
        transactionTemplate.executeWithoutResult(status -> syncAttemptRecorder.markFailed(persistedSyncAttempt.getId(), syncErrorLog));
        SyncAttempt saved = transactionTemplate.execute(status -> syncAttemptRepository.findById(persistedSyncAttempt.getId())).get();
        assertEquals(JobStatus.FAILED, saved.getAttemptStatus());
        assertEquals(SyncErrorCode.SYNC_PROCESS_ERROR, saved.getErrorCode());
        assertNotNull(saved.getFinishedAt());
    }

}