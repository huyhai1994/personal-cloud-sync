package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

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
    private ScheduledSyncJobCreationService syncJobCreationService;

    private final Clock currentTime = Clock.fixed(
            Instant.parse("2026-06-19T10:00:00Z"),
            ZoneOffset.UTC
    );
    @Autowired
    private Clock systemClock;

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

        assertAll(() -> assertNotNull(manualSyncConfig), () -> assertNotNull(firstDueIntervalSyncConfig), () -> assertNotNull(secondDueIntervalSyncConfig),
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
        List<SyncConfig> syncConfigs = syncConfigRepository.findAllById(List.of(firstDueIntervalSyncConfig.getId(), secondDueIntervalSyncConfig.getId()));
        for (SyncConfig dueInvervalSyncConfig : syncConfigs) {
            assertEquals(OffsetDateTime.now(currentTime).plusMinutes(10), dueInvervalSyncConfig.getNextScheduledAt());
        }
    }

    @Test
    void createDueJobs_whenTwoTransactionCreateWithSameConfig_thenOnlyCreateJobsOne() throws InterruptedException, TimeoutException, ExecutionException {
        int numberOfThread = 2;
        CountDownLatch readyLatch = new CountDownLatch(numberOfThread);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);

        Callable<List<Integer>> task = () -> {
            readyLatch.countDown();
            startLatch.await();
            return transactionTemplate.execute(status -> syncJobSchedulerService.createDueJobs());
        };
        Future<List<Integer>> future1 = executorService.submit(task);
        Future<List<Integer>> future2 = executorService.submit(task);

        assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
        startLatch.countDown();

        List<List<Integer>> results = new ArrayList<>();

        for (Future<List<Integer>> future : List.of(future1, future2)) {
            List<Integer> jobIds = future.get(5, TimeUnit.SECONDS);
            assertNotNull(jobIds);
            results.add(jobIds);
        }

        int createdJobIdsCount = results.stream()
                .mapToInt(List::size)
                .sum();

        assertEquals(2, createdJobIdsCount);
        assertEquals(2, syncJobRepository.count());
    }

    @Test
    void createDueJobs_shouldNotRollbackOtherJobs_whenOneConfigHasActiveJob() {
        SyncJob activeSyncJob = new SyncJob();
        activeSyncJob.setFinalStatus(JobStatus.RUNNING);
        activeSyncJob.setSyncConfig(secondDueIntervalSyncConfig);
        transactionTemplate.executeWithoutResult(status ->
                syncJobRepository.save(activeSyncJob));
        assertDoesNotThrow(() ->
                transactionTemplate.executeWithoutResult(status ->
                        syncJobSchedulerService.createDueJobs()
                ));
        SyncConfig updatedfirstSyncConfig = Objects.requireNonNull(transactionTemplate.execute(status ->
                syncConfigRepository.getSyncConfigByIdAndEnabled(firstDueIntervalSyncConfig.getId(), true)
        )).orElseThrow();

        SyncConfig updatedSecondSyncConfig = Objects.requireNonNull(transactionTemplate.execute(status ->
                syncConfigRepository.getSyncConfigByIdAndEnabled(secondDueIntervalSyncConfig.getId(), true)
        )).orElseThrow();

        assertNotEquals(firstDueIntervalSyncConfig.getNextScheduledAt(), updatedfirstSyncConfig.getNextScheduledAt());
        assertEquals(secondDueIntervalSyncConfig.getNextScheduledAt(), updatedSecondSyncConfig.getNextScheduledAt());
    }
}
