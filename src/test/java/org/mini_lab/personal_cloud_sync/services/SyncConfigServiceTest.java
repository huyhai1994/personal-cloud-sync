package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class SyncConfigServiceTest {

    @Autowired
    private SyncConfigService syncConfigService;

    @Test
    void maximum_retry_count_exceed_should_throw_exception() {
        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        createSyncConfigRequest.setMaxRetry((byte) 10);
        assertThrows(MaximumRetryCountExceedException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
    }

}