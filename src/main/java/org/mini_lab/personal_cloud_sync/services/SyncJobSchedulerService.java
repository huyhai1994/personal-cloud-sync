package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.component.NextScheduledAtCalculationStrategy;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobSchedulerService {
    private final SyncConfigRepository syncConfigRepository;

    private final SyncJobSchedulerProperties syncJobSchedulerProperties;

    private final ScheduledSyncJobCreationService scheduledSyncJobCreationService;

    private final Clock systemClock;

    @Transactional
    public List<Integer> createDueJobs() {
        List<Integer> syncJobs = new ArrayList<>();
        int batchSize = syncJobSchedulerProperties.getBatchSize();

        List<SyncConfig> syncConfigs = getDueSyncConfigsForUpdate(batchSize);

        for (SyncConfig syncConfig : syncConfigs) {
            Optional<SyncJob> syncJob = scheduledSyncJobCreationService.createPendingJob(syncConfig);
            syncJob.ifPresent(job -> syncJobs.add(job.getId()));
            syncConfig.setNextScheduledAt(estimateNextScheduledAt(syncConfig));
        }

        return syncJobs;
    }

    private List<SyncConfig> getDueSyncConfigsForUpdate(int batchSize) {
        return syncConfigRepository.findDueNonManualScheduleTypeSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(systemClock),
                PageRequest.of(
                        0,
                        batchSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        );
    }

    private OffsetDateTime estimateNextScheduledAt(SyncConfig syncConfig) {
        NextScheduledAtRequest nextScheduledAtRequest = NextScheduledAtRequest
                .builder()
                .runTime(syncConfig.getRunTime())
                .scheduleInterval(syncConfig.getScheduleInterval())
                .scheduleType(syncConfig.getScheduleType()).build();
        return NextScheduledAtCalculationStrategy.estimateNextScheduledAt(nextScheduledAtRequest, systemClock).orElseThrow();
    }

}
