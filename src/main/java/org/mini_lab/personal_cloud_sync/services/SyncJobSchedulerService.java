package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.component.NextScheduledAtCalculationStrategy;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobSchedulerService {
    private final SyncConfigRepository syncConfigRepository;

    private final SyncJobSchedulerProperties syncJobSchedulerProperties;

    private final Clock systemClock;

    public List<SyncConfig> findDueSyncConfig() {
        int pageSize = syncJobSchedulerProperties.getPollingTime();
        return syncConfigRepository.findDueSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(systemClock),
                PageRequest.of(
                        0,
                        pageSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        );
    }
}
