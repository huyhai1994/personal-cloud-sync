package org.mini_lab.personal_cloud_sync.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.personal_cloud_sync.services.SyncJobProcessor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SyncJobTaskTest {
    @Mock
    SyncJobProcessor syncJobProcessor;

    @Test
    void shouldDelegateToSyncJobProcessor() {
        Integer syncJobId = 100;
        SyncJobTask syncJobTask = new SyncJobTask(syncJobId, syncJobProcessor);
        syncJobTask.run();
        verify(syncJobProcessor).process(syncJobId);
    }


}