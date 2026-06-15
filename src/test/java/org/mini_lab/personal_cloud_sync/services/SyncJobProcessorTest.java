package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.component.IRCloneExecutor;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.mini_lab.personal_cloud_sync.exception.InvalidJobStateTransitionException;
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

    @Test
    void whenProcessAlreadyRanJob_thenThrowExceptionAndNeverSync() throws IOException, InterruptedException {
        // Arrange
        Integer syncJobId = 100;
        String sourcePath = "source/test";
        String targetPath = "target/test";
        when(syncJobProcessorService.markRunning(syncJobId)).thenThrow(new InvalidJobStateTransitionException());
        // Act + assert
        assertThrows(InvalidJobStateTransitionException.class, () -> syncJobProcessor.process(syncJobId));
        // Verify
        verify(rCloneExecutor, never()).sync(sourcePath, targetPath);
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
        when(rCloneExecutor.sync(syncJobContext.sourcePath(), syncJobContext.targetPath())).thenReturn(result);
        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobProcessorService).markRunning(syncJobId);
        verify(rCloneExecutor).sync(sourcePath, targetPath);
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

        RCloneResult result = new RCloneResult();
        result.setExitCode(111);

        when(syncJobProcessorService.markRunning(syncJobId)).thenReturn(syncJobContext);
        when(rCloneExecutor.sync(syncJobContext.sourcePath(), syncJobContext.targetPath())).thenReturn(result);
        // Act
        syncJobProcessor.process(syncJobId);

        // Assert
        verify(syncJobProcessorService).markRunning(syncJobId);
        verify(rCloneExecutor).sync(sourcePath, targetPath);
        verify(syncJobProcessorService).markFailed(syncJobId);
    }
}