package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.SyncJobResponse;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.mini_lab.personal_cloud_sync.support.FakeRCloneConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ManualSyncJobServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ManualSyncJobService manualSyncJobService;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncAttemptRepository syncAttemptRepository;

    private SyncConfig persistedSyncConfig;

    @BeforeEach
    void setUp() {
        persistedSyncConfig = transactionTemplate.execute((status -> {
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setSourcePath(sourcePath.toString());
            syncConfig.setTargetPath(targetPath.toString());
            return syncConfigRepository.save(syncConfig);
        }));
        assertNotNull(persistedSyncConfig);
        assertEquals(sourcePath.toString(), persistedSyncConfig.getSourcePath());
        assertEquals(targetPath.toString(), persistedSyncConfig.getTargetPath());
    }

    @AfterEach
    void tearDown() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createAndDispatch() throws IOException {
        Path sourceFile = sourcePath.resolve("test.txt");
        Path targetFile = targetPath.resolve("test.txt");
        Files.writeString(sourceFile, "Hello World!");
        SyncJobResponse syncJobResponse = manualSyncJobService.createAndDispatch(persistedSyncConfig.getId());
        assertNotNull(syncJobResponse);
        assertNotNull(syncJobResponse.syncJobId());
        assertEquals(JobStatus.PENDING, syncJobResponse.jobStatus());
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.readString(sourceFile), Files.readString(targetFile));
        });
    }


}