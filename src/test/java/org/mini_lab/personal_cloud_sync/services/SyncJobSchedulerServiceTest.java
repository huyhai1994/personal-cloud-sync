package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncJobSchedulerServiceTest {
    @Mock
    SyncConfigRepository syncConfigRepository;

    @Mock
    SyncJobSchedulerProperties syncJobSchedulerProperties;

    @InjectMocks
    SyncJobSchedulerService syncJobSchedulerService;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-11T00:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        syncJobSchedulerService = new SyncJobSchedulerService(syncConfigRepository, syncJobSchedulerProperties, fixedClock);
    }

    @Test
    void findDueSyncConfig_shouldReturnListSyncConfig() {
        SyncConfig dueConfig = new SyncConfig();
        dueConfig.setSourcePath("/source/test1");
        dueConfig.setTargetPath("/target/test1");
        dueConfig.setScheduleType(ScheduleType.INTERVAL);
        dueConfig.setScheduleInterval((short) 30);
        dueConfig.setNextScheduledAt(
                OffsetDateTime.now(fixedClock).minusMinutes(10)
        );
        int pageSize = 10;
        when(syncJobSchedulerProperties
                .getPollingTime()).thenReturn(pageSize);
        when(syncConfigRepository.findDueSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(fixedClock),
                PageRequest.of(
                        0,
                        pageSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        )).thenReturn(List.of(dueConfig));

        assertEquals(ScheduleType.INTERVAL, syncJobSchedulerService.findDueSyncConfig().get(0).getScheduleType());
    }


}