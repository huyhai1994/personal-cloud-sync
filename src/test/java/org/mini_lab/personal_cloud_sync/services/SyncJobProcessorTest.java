package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobProcessorTest {

    @InjectMocks
    SyncJobProcessor syncJobProcessor;

    @Mock
    SyncJobProcessorService syncJobProcessorService;

    @Mock
    IRCloneExecutor rCloneExecutor;

    @Mock
    SyncConfigValidator syncConfigValidator;

    @Test
    void whenProcessAlreadyRanJob_thenThrowExceptionAndNeverSync() throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        String sourcePath = "source/test";
        String targetPath = "target/test";
        SyncJobContext syncJobContext = SyncJobContext.builder()
                .syncJobId(100)
                .sourcePath(sourcePath)
                .targetPath(targetPath)
                .build();
        when(syncJobProcessorService.markRunning(syncJobId)).thenThrow(new InvalidJobStateTransitionException());
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
        String sourcePath = "source/test";
        String targetPath = "target/test";
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncAttemptId, sourcePath, targetPath);

        RCloneResult result = new RCloneResult();
        result.setExitCode(0);


        when(syncJobProcessorService.markRunning(syncJobId)).thenReturn(syncJobContext);
        when(rCloneExecutor.sync(syncJobContext)).thenReturn(result);
        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobProcessorService).markRunning(syncJobId);
        verify(rCloneExecutor).sync(syncJobContext);
        verify(syncJobProcessorService).markSuccess(syncJobContext);
    }

    @Test
    void whenSyncFail_thenMarkFailedMethodCalled() throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        String sourcePath = "source/test";
        String targetPath = "target/test";
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, sourcePath, targetPath);

        SyncErrorLog syncErrorLog = new SyncErrorLog(SyncErrorCode.SYNC_PROCESS_ERROR, "Rclone process finished with non-zero exit code");
        RCloneResult result = new RCloneResult();
        result.setExitCode(111);

        when(syncJobProcessorService.markRunning(syncJobId)).thenReturn(syncJobContext);
        when(rCloneExecutor.sync(syncJobContext)).thenReturn(result);
        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobProcessorService).markRunning(syncJobId);
        verify(rCloneExecutor).sync(syncJobContext);
        verify(syncJobProcessorService).markFailed(syncJobContext, syncErrorLog);
    }

    @Test
    void whenValidateFail_thenSyncProcessShouldNotCalled() throws IOException, InterruptedException {
        Integer syncJobId = 100;
        Integer syncJobAttemptId = 100;
        String sourcePath = "source/test";
        String targetPath = "target/test";
        SyncJobContext syncJobContext = new SyncJobContext(syncJobId, syncJobAttemptId, sourcePath, targetPath);

        when(syncJobProcessorService.markRunning(syncJobId)).thenReturn(syncJobContext);
        doThrow(new LocalPathIsNotDirectory())
                .when(syncConfigValidator)
                .validateSourcePath(sourcePath);        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobProcessorService).markRunning(syncJobId);
        verify(syncConfigValidator, never())
                .validateTargetPath(anyString());
        verify(rCloneExecutor, never())
                .sync(any());
        verify(syncJobProcessorService).markFailed(
                eq(syncJobContext),
                argThat(error ->
                        error.syncErrorCode() == SyncErrorCode.VALIDATION_ERROR
                )
        );
        verify(syncJobProcessorService, never())
                .markSuccess(any());
    }
}