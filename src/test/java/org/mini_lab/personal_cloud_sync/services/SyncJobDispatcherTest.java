package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.RejectedExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobDispatcherTest {
    @InjectMocks
    SyncJobDispatcher syncJobDispatcher;

    @Mock
    TaskExecutor syncJobExecutor;

    @Mock
    SyncJobProcessor syncJobProcessor;

    @Mock
    SyncJobProcessorService syncJobProcessorService;

    @Test
    void whenExecutorRejectTask_thenMarkSubmitFailed() {
        // Arrange
        Integer syncJobId = 1;

        doThrow(new RejectedExecutionException())
                .when(syncJobExecutor)
                .execute(any(Runnable.class));

        // Act
        syncJobDispatcher.dispatch(syncJobId);

        // Assert
        verify(syncJobProcessorService).markSubmitted(syncJobId);
        verify(syncJobExecutor).execute(any(Runnable.class));
        verify(syncJobProcessorService).markSubmitFailed(syncJobId);
        verify(syncJobProcessor, never()).process(any());
    }

    @Test
    void whenDispatchSuccess_thenSubmitProcessorTask() {
        // Arrange
        Integer syncJobId = 1;
        ArgumentCaptor<Runnable> taskCaptor =
                ArgumentCaptor.forClass(Runnable.class);

        // Act
        syncJobDispatcher.dispatch(syncJobId);

        // Assert
        verify(syncJobExecutor).execute(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        task.run();

        verify(syncJobProcessor).process(syncJobId);
        verify(syncJobProcessorService).markSubmitted(syncJobId);
        verify(syncJobProcessorService, never()).markSubmitFailed(any());
    }
}