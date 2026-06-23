package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.exception.SyncConfigNotFoundException;
import org.mini_lab.personal_cloud_sync.exception.SyncJobAlreadyActiveException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobCreationServiceTest {

    @InjectMocks
    ManualSyncJobCreationService syncJobCreationService;

    @Mock
    SyncJobRepository syncJobRepository;

    @Mock
    SyncConfigRepository syncConfigRepository;

    @Test
    void createPendingJob_whenSyncConfigValidAndSyncJobNotExistedYet_shouldCreateJob() {
        short syncConfigId = (short) 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        syncConfig.setId(syncConfigId);

        when(syncConfigRepository.getSyncConfigByIdAndEnabled(syncConfigId, Boolean.TRUE)).thenReturn(Optional.of(syncConfig));

        syncJobCreationService.createPendingJob(syncConfigId);

        ArgumentCaptor<SyncJob> captor = ArgumentCaptor.forClass(SyncJob.class);

        verify(syncJobRepository).save(captor.capture());

        SyncJob savedJob = captor.getValue();

        assertSame(syncConfig, savedJob.getSyncConfig());
        assertEquals(JobStatus.PENDING, savedJob.getFinalStatus());
    }

    @Test
    void createPendingJob_whenSyncConfigNotFound_shouldThrowNotFoundException() {
        short syncConfigId = (short) 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        syncConfig.setId(syncConfigId);

        when(syncConfigRepository.getSyncConfigByIdAndEnabled(syncConfigId, Boolean.TRUE)).thenReturn(Optional.empty());

        assertThrows(SyncConfigNotFoundException.class, () -> syncJobCreationService.createPendingJob(syncConfigId));

        verify(syncJobRepository, never()).save(any());

    }

    @Test
    void createPendingJob_whenSyncConfigDisabled_shouldThrowNotFoundException() {
        short syncConfigId = (short) 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        syncConfig.setId(syncConfigId);
        syncConfig.setEnabled(Boolean.FALSE);

        when(syncConfigRepository.getSyncConfigByIdAndEnabled(syncConfigId, Boolean.TRUE)).thenReturn(Optional.empty());

        assertThrows(SyncConfigNotFoundException.class, () -> syncJobCreationService.createPendingJob(syncConfigId));

        verify(syncJobRepository, never()).save(any());

    }

    @Test
    void createPendingJob_whenJobAlreadyExisted_shouldThrowSyncJobAlreadyRunningException() {
        short syncConfigId = (short) 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        syncConfig.setId(syncConfigId);

        when(syncConfigRepository.getSyncConfigByIdAndEnabled(syncConfigId, Boolean.TRUE)).thenReturn(Optional.of(syncConfig));

        when(syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(syncConfigId, List.of(JobStatus.PENDING, JobStatus.RUNNING, JobStatus.SUBMITTED))).thenReturn(Boolean.TRUE);

        assertThrows(SyncJobAlreadyActiveException.class, () -> syncJobCreationService.createPendingJob(syncConfigId));

        verify(syncJobRepository, never()).save(any());

    }
}