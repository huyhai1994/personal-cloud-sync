package org.mini_lab.personal_cloud_sync.repositories;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncConfigRepositoryTest {

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Test
    void persist_sync_config_should_success() {
        SyncConfig syncConfig = new SyncConfig();

        syncConfig.setMountPath("/mnt/test");
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");

        SyncConfig syncConfig1 = syncConfigRepository.saveAndFlush(syncConfig);

        assertNotNull(syncConfig1.getId());
    }
}