package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
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
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobSchedulerServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private SyncAttemptRepository syncAttemptRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncJobSchedulerService syncJobSchedulerService;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    private SyncConfig firstDueIntervalSyncConfig;
    private SyncConfig secondDueIntervalSyncConfig;
    private SyncConfig manualSyncConfig;
    private SyncConfig futureIntervalSyncConfig;
    private SyncConfig disabledDueIntervalSyncConfig;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SyncJobSchedulerProperties syncJobSchedulerProperties;

    @Autowired
    private SyncJobCreationService syncJobCreationService;

    private final Clock currentTime = Clock.fixed(
            Instant.parse("2026-06-19T10:00:00Z"),
            ZoneOffset.UTC
    );

    @AfterEach
    void tearDown() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        short scheduledIntervalInMinutes = (short) 10;
        OffsetDateTime now = OffsetDateTime.now(currentTime);

        syncJobSchedulerService = new SyncJobSchedulerService(
                syncConfigRepository,
                syncJobSchedulerProperties,
                syncJobCreationService,
                currentTime
        );

        manualSyncConfig = saveSyncConfig(
                sourcePath.resolve("manual").toString(),
                targetPath.resolve("manual").toString(),
                ScheduleType.MANUAL,
                null,
                null,
                true
        );

        firstDueIntervalSyncConfig = saveSyncConfig(
                sourcePath.resolve("due-1").toString(),
                targetPath.resolve("due-1").toString(),
                ScheduleType.INTERVAL,
                scheduledIntervalInMinutes,
                now.minusMinutes(10),
                true
        );

        secondDueIntervalSyncConfig = saveSyncConfig(
                sourcePath.resolve("due-2").toString(),
                targetPath.resolve("due-2").toString(),
                ScheduleType.INTERVAL,
                scheduledIntervalInMinutes,
                now.minusMinutes(10),
                true
        );

        futureIntervalSyncConfig = saveSyncConfig(
                sourcePath.resolve("future").toString(),
                targetPath.resolve("future").toString(),
                ScheduleType.INTERVAL,
                scheduledIntervalInMinutes,
                now.plusMinutes(10),
                true
        );

        disabledDueIntervalSyncConfig = saveSyncConfig(
                sourcePath.resolve("disabled").toString(),
                targetPath.resolve("disabled").toString(),
                ScheduleType.INTERVAL,
                scheduledIntervalInMinutes,
                now.minusMinutes(10),
                false
        );

        assertAll( () -> assertNotNull(manualSyncConfig), () -> assertNotNull(firstDueIntervalSyncConfig), () -> assertNotNull(secondDueIntervalSyncConfig),
                () -> assertNotNull(futureIntervalSyncConfig),
                () -> assertNotNull(disabledDueIntervalSyncConfig)
        );
    }

    private SyncConfig saveSyncConfig(
            String sourcePath,
            String targetPath,
            ScheduleType scheduleType,
            Short scheduleInterval,
            OffsetDateTime nextScheduledAt,
            boolean enabled
    ) {
        return transactionTemplate.execute(status -> {
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setSourcePath(sourcePath);
            syncConfig.setTargetPath(targetPath);
            syncConfig.setScheduleType(scheduleType);
            syncConfig.setScheduleInterval(scheduleInterval);
            syncConfig.setNextScheduledAt(nextScheduledAt);
            syncConfig.setEnabled(enabled);
            return syncConfigRepository.save(syncConfig);
        });
    }

    @Test
    void createDueJobs_shouldCreateSuccessfullyTwoValidSyncJobs() {
        List<SyncConfig> syncConfig =
                transactionTemplate.execute(status ->
                        syncConfigRepository.findAll()
                );

        assertEquals(5, syncConfig.size());

        List<Integer> jobIds =
                transactionTemplate.execute(status ->
                        syncJobSchedulerService.createDueJobs()
                );

        assertNotNull(jobIds);
        assertEquals(2, jobIds.size());
    }
}
