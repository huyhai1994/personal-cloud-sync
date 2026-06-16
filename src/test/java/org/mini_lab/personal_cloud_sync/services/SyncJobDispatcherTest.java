package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobDispatcherTest {
    @InjectMocks
    SyncJobDispatcher syncJobDispatcher;

    @Mock
    ExecutorService syncJobExecutor;

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
                .submit(any(Runnable.class));

        // Act
        syncJobDispatcher.dispatch(syncJobId);

        // Assert
        verify(syncJobExecutor).submit(any(Runnable.class));
        verify(syncJobProcessorService).markSubmitFailed(syncJobId);
        verify(syncJobProcessor, never()).process(any());
        verify(syncJobProcessorService, never()).markSubmitted(any());
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
        verify(syncJobExecutor).submit(taskCaptor.capture());

        Runnable task = taskCaptor.getValue();
        task.run();

        verify(syncJobProcessor).process(syncJobId);
        verify(syncJobProcessorService).markSubmitted(syncJobId);
        verify(syncJobProcessorService, never()).markSubmitFailed(any());
    }
}