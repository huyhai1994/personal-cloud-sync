package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.component.SyncConfigValidator;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorLog;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobProcessorTest {

    @InjectMocks
    SyncJobProcessor syncJobProcessor;

    @Mock
    SyncJobStateManager syncJobStateManager;

    @Mock
    ScheduledThreadPoolExecutor heartbeatExecutor;

    @Mock
    private ScheduledFuture<?> heartbeatTask;

    @Mock
    IRCloneExecutor rCloneExecutor;

    @Mock
    SyncConfigValidator syncConfigValidator;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @Test
    void whenProcessAlreadyRanJob_thenThrowExceptionAndNeverSync() throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        SyncJobContext syncJobContext = SyncJobContext.builder()
                .syncJobId(100)
                .sourcePath(sourcePath.toString())
                .targetPath(targetPath.toString())
                .build();
        when(syncJobStateManager.markRunning(syncJobId)).thenThrow(new InvalidJobStateTransitionException());
        // Act + assert
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessor.process(syncJobId));
        // Verify
        verify(rCloneExecutor, never()).sync(syncJobContext);
    }

    @Test
    void whenSyncSuccess_thenMarkSuccessMethodCalled() throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        Integer syncAttemptId = 100;
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncAttemptId, sourcePath.toString(), targetPath.toString());

        RCloneResult result = new RCloneResult();
        result.setExitCode(0);

        when(syncJobStateManager.markRunning(syncJobId)).thenReturn(syncJobContext);
        doReturn(heartbeatTask)
                .when(heartbeatExecutor)
                .scheduleAtFixedRate(
                        any(Runnable.class),
                        eq(5L),
                        eq(5L),
                        eq(TimeUnit.SECONDS)
                );
        when(rCloneExecutor.sync(syncJobContext)).thenReturn(result);
        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobStateManager).markRunning(syncJobId);
        verify(heartbeatExecutor).scheduleAtFixedRate(
                any(Runnable.class),
                eq(5L),
                eq(5L),
                eq(TimeUnit.SECONDS)
        );
        verify(rCloneExecutor).sync(syncJobContext);
        verify(syncJobStateManager).markSuccess(syncJobContext);
        verify(heartbeatTask).cancel(true);
    }

    @Test
    void whenSyncFail_thenMarkFailedMethodCalled_andHeartbeatCancelled()
            throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;

        SyncJobContext syncJobContext = new SyncJobContext(
                syncJobId,
                syncJobAttemptId,
                sourcePath.toString(),
                targetPath.toString()
        );

        SyncErrorLog syncErrorLog = new SyncErrorLog(
                SyncErrorCode.SYNC_PROCESS_ERROR,
                "Rclone process finished with non-zero exit code"
        );

        RCloneResult result = new RCloneResult();
        result.setExitCode(111);

        when(syncJobStateManager.markRunning(syncJobId))
                .thenReturn(syncJobContext);

        doReturn(heartbeatTask)
                .when(heartbeatExecutor)
                .scheduleAtFixedRate(
                        any(Runnable.class),
                        eq(5L),
                        eq(5L),
                        eq(TimeUnit.SECONDS)
                );

        when(rCloneExecutor.sync(syncJobContext))
                .thenReturn(result);

        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobStateManager).markRunning(syncJobId);
        verify(heartbeatExecutor).scheduleAtFixedRate(
                any(Runnable.class),
                eq(5L),
                eq(5L),
                eq(TimeUnit.SECONDS)
        );
        verify(rCloneExecutor).sync(syncJobContext);
        verify(syncJobStateManager).markFailed(syncJobContext, syncErrorLog);
        verify(heartbeatTask).cancel(true);
    }

    @Test
    void whenValidateFail_thenSyncProcessShouldNotCalled() throws IOException, InterruptedException {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, sourcePath.toString(), targetPath.toString());

        when(syncJobStateManager.markRunning(syncJobId)).thenReturn(syncJobContext);
        doReturn(heartbeatTask)
                .when(heartbeatExecutor)
                .scheduleAtFixedRate(
                        any(Runnable.class),
                        eq(5L),
                        eq(5L),
                        eq(TimeUnit.SECONDS)
                );
        doThrow(new LocalPathIsNotDirectory())
                .when(syncConfigValidator)
                .validateSourcePath(sourcePath.toString());        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobStateManager).markRunning(syncJobId);
        verify(heartbeatExecutor).scheduleAtFixedRate(
                any(Runnable.class),
                eq(5L),
                eq(5L),
                eq(TimeUnit.SECONDS)
        );
        verify(syncConfigValidator, never())
                .validateTargetPath(anyString());
        verify(rCloneExecutor, never())
                .sync(any());
        verify(syncJobStateManager).markFailed(
                eq(syncJobContext),
                argThat(error ->
                        error.syncErrorCode() == SyncErrorCode.VALIDATION_ERROR
                )
        );
        verify(syncJobStateManager, never())
                .markSuccess(any());
        verify(heartbeatTask).cancel(true);
    }
}