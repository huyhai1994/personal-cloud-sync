package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SyncJobProcessorServiceTest {
    @Mock
    SyncJobRepository syncJobRepository;

    @Mock
    SyncAttemptRecorder syncAttemptRecorder;

    @InjectMocks
    SyncJobProcessorService syncJobProcessorService;

    @Test
    void whenUpdatedFail_fromPendingToRunningState_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markRunning(syncJobId));
        verify(syncJobRepository).updateStatusIfCurrentStatus(
                syncJobId,
                JobStatus.PENDING,
                JobStatus.RUNNING
        );
        verify(syncJobRepository, never()).getSyncJobById(anyInt());
        verifyNoMoreInteractions(syncJobRepository);
    }

    @Test
    void whenUpdatedFail_fromRunningToFailed_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, "/source/test", "/target/test");
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markFailed(syncJobContext));
    }

    @Test
    void whenUpdatedFail_fromRunningToSuccess_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, "/source/test", "/target/test");
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markSuccess(syncJobContext));
    }

    @Test
    void whenUpdatedTrue_fromPendingToRunningState_shouldReturnSyncConfigContext() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");

        SyncJob syncJob = new SyncJob();
        syncJob.setId(syncJobId);
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);

        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.RUNNING)).thenReturn(1);
        when(syncJobRepository.getSyncJobById(syncJobId)).thenReturn(Optional.of(syncJob));
        when(syncAttemptRecorder.startAttempt(syncJob)).thenReturn(syncJobAttemptId);
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, "/source/test", "/target/test");
        assertEquals(syncJobContext, syncJobProcessorService.markRunning(syncJobId));
    }
}