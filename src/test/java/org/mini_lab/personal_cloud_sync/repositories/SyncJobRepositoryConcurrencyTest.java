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

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        syncJobRepository.deleteAll();
        syncConfigRepository.deleteAll();
    }

    @Test
    void updateStatusIfCurrentStatus_shouldReturnOne_WhenTwoThreadUpdateSameTime() throws InterruptedException, ExecutionException {
        int numberOfThread = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThread);
        CountDownLatch startLatch = new CountDownLatch(1);

        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig persistedSyncConfig =
                syncConfigRepository.saveAndFlush(syncConfig);

        SyncJob firstSyncJob = new SyncJob();
        firstSyncJob.setSyncConfig(persistedSyncConfig);
        firstSyncJob.setFinalStatus(JobStatus.PENDING);

        SyncJob savedSyncJob = syncJobRepository.saveAndFlush(firstSyncJob);
        Integer syncJobId = savedSyncJob.getId();

        Callable<Integer> task = () -> {
            readyLatch.countDown();
            startLatch.await();
            return transactionTemplate.execute(status ->
                    syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING)
            );
        };

        Future<Integer> future1 = executorService.submit(task);
        Future<Integer> future2 = executorService.submit(task);

        readyLatch.await();
        startLatch.countDown();

        Integer result1 = future1.get();
        Integer result2 = future2.get();
        executorService.shutdown();

        assertEquals(1, result1 + result2);

        SyncJob reloaded = syncJobRepository.findById(syncJobId).orElseThrow();
        assertEquals(JobStatus.RUNNING, reloaded.getFinalStatus());


    }
}

