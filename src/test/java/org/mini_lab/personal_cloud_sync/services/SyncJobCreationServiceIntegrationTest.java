package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.exception.SyncJobAlreadyActiveException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobCreationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private SyncJobCreationService syncJobCreationService;

    private SyncConfig persistedSyncConfig;

    @BeforeEach
    void setUp() {
        persistedSyncConfig = transactionTemplate.execute((status -> {
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setSourcePath("/source/test");
            syncConfig.setTargetPath("/target/test");
            return syncConfigRepository.save(syncConfig);
        }));
        assertNotNull(persistedSyncConfig);
        assertEquals("/source/test", persistedSyncConfig.getSourcePath());
        assertEquals("/target/test", persistedSyncConfig.getTargetPath());
    }

    @AfterEach
    void tearDown() {
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    @Test
    void createPendingJob_whenCalledConcurrently_shouldCreateOnlyOneJob() throws Exception {
        int threadCount = 2;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        try {
            Callable<SyncJob> task = () -> {
                readyLatch.countDown();
                startLatch.await();
                return syncJobCreationService.createPendingJob(persistedSyncConfig.getId());
            };

            Future<SyncJob> future1 = executorService.submit(task);
            Future<SyncJob> future2 = executorService.submit(task);

            assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
            startLatch.countDown();

            int successCount = 0;
            int alreadyRunningCount = 0;

            for (Future<SyncJob> future : List.of(future1, future2)) {
                try {
                    SyncJob job = future.get(5, TimeUnit.SECONDS);
                    assertNotNull(job);
                    successCount++;
                } catch (ExecutionException e) {
                    assertInstanceOf(SyncJobAlreadyActiveException.class, e.getCause());
                    alreadyRunningCount++;
                }
            }

            assertEquals(1, successCount);
            assertEquals(1, alreadyRunningCount);

            List<SyncJob> jobs = syncJobRepository.findAll();
            assertEquals(1, jobs.size());
            assertEquals(JobStatus.PENDING, jobs.get(0).getFinalStatus());

        } finally {
            executorService.shutdownNow();
        }
    }

}
