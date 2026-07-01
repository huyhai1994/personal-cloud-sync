package org.mini_lab.personal_cloud_sync.scheduler;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.services.SyncJobRecoveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RecoveryScheduler {
    private final SyncJobRecoveryService syncJobRecoveryService;

    @Scheduled(
            fixedRateString = "${recovery-scheduler.running-interval}",
            timeUnit = TimeUnit.SECONDS
    )
    @Timed(
            value = "recovery.scheduler.recover.timed.out.running.jobs",
            description = "Time taken to recover timed out running jobs"
    )
    public void recoverTimedOutRunningJobs() {
        syncJobRecoveryService.findAndUpdateTimedOutRunningJobs();
    }
}
