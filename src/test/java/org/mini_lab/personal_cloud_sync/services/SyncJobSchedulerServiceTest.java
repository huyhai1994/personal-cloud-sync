package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.configuration.SyncJobSchedulerProperties;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.exception.SyncConfigNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobSchedulerServiceTest {
    @Mock
    SyncConfigRepository syncConfigRepository;

    @Mock
    SyncJobSchedulerProperties syncJobSchedulerProperties;

    @Mock
    ScheduledSyncJobCreationService scheduledSyncJobCreationService;

    @InjectMocks
    SyncJobSchedulerService syncJobSchedulerService;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-11T00:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        syncJobSchedulerService = new SyncJobSchedulerService(
                syncConfigRepository,
                syncJobSchedulerProperties,
                scheduledSyncJobCreationService,
                fixedClock
        );
    }

    @Test
    void createDueJobs_whenDueConfigsExists_shouldCreateListOfSyncJob() {
        SyncConfig dueConfig = new SyncConfig();
        dueConfig.setSourcePath("/source/test1");
        dueConfig.setTargetPath("/target/test1");
        dueConfig.setScheduleType(ScheduleType.INTERVAL);
        dueConfig.setScheduleInterval((short) 30);
        dueConfig.setNextScheduledAt(
                OffsetDateTime.now(fixedClock).minusMinutes(10)
        );

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(dueConfig);
        int batchSize = 10;
        when(syncJobSchedulerProperties
                .getBatchSize()).thenReturn(batchSize);
        when(syncConfigRepository.findDueNonManualScheduleTypeSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(fixedClock),
                PageRequest.of(
                        0,
                        batchSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        )).thenReturn(List.of(dueConfig));

        when(scheduledSyncJobCreationService.createPendingJob(dueConfig)).thenReturn(Optional.of(syncJob));

        syncJobSchedulerService.createDueJobs();

        verify(scheduledSyncJobCreationService).createPendingJob(dueConfig);

    }

    @Test
    void createDueJobs_whenDueConfigsNotExists_listOfDueJobShouldBeEmpty() {
        SyncConfig syncConfig = new SyncConfig();
        int batchSize = 10;
        when(syncJobSchedulerProperties
                .getBatchSize()).thenReturn(batchSize);
        when(syncConfigRepository.findDueNonManualScheduleTypeSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(fixedClock),
                PageRequest.of(
                        0,
                        batchSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        )).thenReturn(List.of());

        assertEquals(List.of(), syncJobSchedulerService.createDueJobs());
        verify(scheduledSyncJobCreationService, never()).createPendingJob(syncConfig);

    }

    @Test
    void createDueJobs_whenCreatingPendingJobFailed_listOfDueJobShouldBeEmpty() {
        short dueConfigId = (short) 100;
        int batchSize = 10;
        SyncConfig dueConfig = new SyncConfig();
        dueConfig.setSourcePath("/source/test1");
        dueConfig.setTargetPath("/target/test1");
        dueConfig.setScheduleType(ScheduleType.INTERVAL);
        dueConfig.setScheduleInterval((short) 30);
        dueConfig.setNextScheduledAt(
                OffsetDateTime.now(fixedClock).minusMinutes(10)
        );
        dueConfig.setId(dueConfigId);
        when(syncJobSchedulerProperties
                .getBatchSize()).thenReturn(batchSize);
        when(syncConfigRepository.findDueNonManualScheduleTypeSyncConfigs(
                ScheduleType.MANUAL,
                OffsetDateTime.now(fixedClock),
                PageRequest.of(
                        0,
                        batchSize,
                        Sort.by("nextScheduledAt").ascending()
                )
        )).thenReturn(List.of(dueConfig));
        when(scheduledSyncJobCreationService.createPendingJob(dueConfig)).thenReturn(Optional.empty());
        assertEquals(List.of(), syncJobSchedulerService.createDueJobs());

    }
}