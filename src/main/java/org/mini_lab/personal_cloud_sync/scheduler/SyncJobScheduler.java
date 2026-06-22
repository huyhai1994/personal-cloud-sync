package org.mini_lab.personal_cloud_sync.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.services.SyncJobDispatcher;
import org.mini_lab.personal_cloud_sync.services.SyncJobSchedulerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SyncJobScheduler {

    private final SyncJobSchedulerService syncJobSchedulerService;
    private final SyncJobDispatcher dispatcher;

    @Scheduled(fixedRate = 30)
    public void processDueSyncConfig() {
        List<Integer> syncJobIds = syncJobSchedulerService.createDueJobs();
        for (Integer jobId : syncJobIds) {
            dispatcher.dispatch(jobId);
        }
    }


}
