package org.mini_lab.personal_cloud_sync.services;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SyncJobProcessorServiceTest {
    @Mock
    SyncJobRepository syncJobRepository;

    @Mock
    SyncAttemptRecorder syncAttemptRecorder;

    @InjectMocks
    SyncJobProcessorService syncJobProcessorService;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-24T10:00:00Z"),
            ZoneOffset.UTC
    );

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @BeforeEach
    void setUp() {
        syncJobProcessorService = new SyncJobProcessorService(syncJobRepository, syncAttemptRecorder, fixedClock);
    }

    @Test
    void whenUpdatedFail_fromPendingToSubmitFail_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.SUBMIT_FAILED)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markSubmitFailed(syncJobId));
        verify(syncJobRepository).updateStatusIfCurrentStatus(
                syncJobId,
                JobStatus.PENDING,
                JobStatus.SUBMIT_FAILED
        );
    }

    @Test
    void whenUpdatedSuccess_fromPendingToSubmitFail_shouldNotThrow() {
        Integer syncJobId = 100;
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.PENDING, JobStatus.SUBMIT_FAILED)).thenReturn(1);
        assertDoesNotThrow(() -> syncJobProcessorService.markSubmitFailed(syncJobId));
        verify(syncJobRepository).updateStatusIfCurrentStatus(
                syncJobId,
                JobStatus.PENDING,
                JobStatus.SUBMIT_FAILED
        );
    }

    @Test
    void whenUpdatedFail_fromPendingToRunningState_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        when(syncJobRepository.markRunningIfSubmitted(syncJobId, JobStatus.RUNNING, JobStatus.SUBMITTED, OffsetDateTime.now(fixedClock))).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markRunning(syncJobId));
        verify(syncJobRepository).markRunningIfSubmitted(
                syncJobId,
                JobStatus.RUNNING,
                JobStatus.SUBMITTED,
                OffsetDateTime.now(fixedClock)
        );
        verify(syncJobRepository, never()).getSyncJobById(anyInt());
        verifyNoMoreInteractions(syncJobRepository);
    }

    @Test
    void whenUpdatedFail_fromRunningToFailed_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncJobContext syncJobContext = getSyncJobContext(syncJobId, syncJobAttemptId);
        SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.SYNC_PROCESS_ERROR, "Rclone process finished with non-zero exit code");
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.FAILED)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markFailed(syncJobContext, syncErrorLog));
    }

    private @NotNull SyncJobContext getSyncJobContext(Integer syncJobId, Integer syncJobAttemptId) {
        return new SyncJobContext(syncJobId, syncJobAttemptId, sourcePath.toString(), targetPath.toString());
    }

    @Test
    void whenUpdatedFail_fromRunningToSuccess_shouldThrowInvalidJobStateTransitionException() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncJobContext syncJobContext = getSyncJobContext(syncJobId, syncJobAttemptId);
        when(syncJobRepository.updateStatusIfCurrentStatus(syncJobId, JobStatus.RUNNING, JobStatus.SUCCESS)).thenReturn(0);
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessorService.markSuccess(syncJobContext));
    }

    @Test
    void whenUpdatedTrue_fromSubmittedToRunningState_shouldReturnSyncConfigContext() {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath.toString());
        syncConfig.setTargetPath(targetPath.toString());

        SyncJob syncJob = new SyncJob();
        syncJob.setId(syncJobId);
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(JobStatus.SUBMITTED);

        when(syncJobRepository.markRunningIfSubmitted(syncJobId, JobStatus.RUNNING, JobStatus.SUBMITTED, OffsetDateTime.now(fixedClock))).thenReturn(1);
        when(syncJobRepository.getSyncJobById(syncJobId)).thenReturn(Optional.of(syncJob));
        when(syncAttemptRecorder.startAttempt(syncJob)).thenReturn(syncJobAttemptId);
        SyncJobContext syncJobContext = getSyncJobContext(syncJobId, syncJobAttemptId);
        assertEquals(syncJobContext, syncJobProcessorService.markRunning(syncJobId));
    }
}