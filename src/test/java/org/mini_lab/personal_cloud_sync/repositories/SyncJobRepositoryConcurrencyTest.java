package org.mini_lab.personal_cloud_sync.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobRepositoryConcurrencyTest {

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    void updateStatusIfCurrentStatus_shouldAllowOneThreadToUpdate_WhenTwoThreadUpdateSameTime() throws InterruptedException, ExecutionException, TimeoutException {
        int numberOfThread = 2;
        CountDownLatch readyLatch = new CountDownLatch(numberOfThread);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        try {
            SyncConfig persistedSyncConfig = transactionTemplate.execute(status -> {
                SyncConfig syncConfig = new SyncConfig();
                syncConfig.setSourcePath("/source/test");
                syncConfig.setTargetPath("/target/test");
                return syncConfigRepository.save(syncConfig);
            });

            Objects.requireNonNull(persistedSyncConfig);
            SyncJob savedSyncJob = transactionTemplate.execute(status -> {
                SyncJob firstSyncJob = new SyncJob();
                firstSyncJob.setSyncConfig(persistedSyncConfig);
                firstSyncJob.setFinalStatus(JobStatus.PENDING);
                return syncJobRepository.saveAndFlush(firstSyncJob);
            });

            Objects.requireNonNull(savedSyncJob);
            Integer syncJobId = savedSyncJob.getId();

            Callable<Integer> task = () -> {
                readyLatch.countDown();
                startLatch.await();
                return transactionTemplate.execute(status -> syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING));
            };

            Future<Integer> future1 = executorService.submit(task);
            Future<Integer> future2 = executorService.submit(task);

            assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
            startLatch.countDown();

            Integer result1 = future1.get(5, TimeUnit.SECONDS);
            Integer result2 = future2.get(5, TimeUnit.SECONDS);

            assertEquals(1, result1 + result2);

            SyncJob reloaded = syncJobRepository.findById(syncJobId).orElseThrow();
            assertEquals(JobStatus.RUNNING, reloaded.getFinalStatus());
        } finally {
            executorService.shutdown();
        }


    }
}

