package org.mini_lab.personal_cloud_sync.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncConfigRepositoryTest {

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @BeforeEach
    void setUp() {
        syncConfigRepository.deleteAll();
    }

    @Test
    void persist_sync_config_should_success() {
        SyncConfig syncConfig = new SyncConfig();

        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig syncConfig1 = syncConfigRepository.saveAndFlush(syncConfig);
        assertNotNull(syncConfig1.getId());
    }

    @Test
    void persist_invalid_sync_config_should_throw_exception() {
        SyncConfig syncConfig = new SyncConfig();
        assertThrows(DataIntegrityViolationException.class, () -> syncConfigRepository.saveAndFlush(syncConfig));
    }

    @Test
    void persist_sync_config_created_at_auto_generated() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath("/source/test");
        syncConfig.setTargetPath("/target/test");
        SyncConfig syncConfig1 = syncConfigRepository.saveAndFlush(syncConfig);
        assertNotNull(syncConfig1.getCreatedAt());
    }

    @Test
    void source_path_and_target_path_should_be_unique() {
        SyncConfig enabledConfig1 = new SyncConfig();
        enabledConfig1.setSourcePath("/source/test/1");
        enabledConfig1.setTargetPath("/target/test/1");

        SyncConfig enabledConfig2 = new SyncConfig();
        enabledConfig2.setEnabled(false);
        enabledConfig2.setSourcePath("/source/test/1");
        enabledConfig2.setTargetPath("/target/test/1");
        syncConfigRepository.saveAndFlush(enabledConfig1);

        assertThrows(DataIntegrityViolationException.class, () -> syncConfigRepository.saveAndFlush(enabledConfig2));


    }

    @Test
    void get_sync_config_by_enabled_should_return_only_enabled_configs() {

        SyncConfig enabledConfig = new SyncConfig();
        enabledConfig.setSourcePath("/source/test");
        enabledConfig.setTargetPath("/target/test");

        SyncConfig disabledConfig = new SyncConfig();
        disabledConfig.setEnabled(false);
        disabledConfig.setSourcePath("/source/test/disabled");
        disabledConfig.setTargetPath("/target/test/disabled");

        syncConfigRepository.saveAndFlush(enabledConfig);
        syncConfigRepository.saveAndFlush(disabledConfig);

        List<SyncConfig> result;
        result = syncConfigRepository.getSyncConfigByEnabled(
                true,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());

    }

    @Test
    void get_sync_config_by_enabled_should_apply_pagination() {

        SyncConfig enabledConfig1 = new SyncConfig();
        enabledConfig1.setSourcePath("/source/test/1");
        enabledConfig1.setTargetPath("/target/test/1");

        SyncConfig enabledConfig2 = new SyncConfig();
        enabledConfig2.setEnabled(false);
        enabledConfig2.setSourcePath("/source/test/2");
        enabledConfig2.setTargetPath("/target/test/2");

        syncConfigRepository.saveAndFlush(enabledConfig1);
        syncConfigRepository.saveAndFlush(enabledConfig2);

        List<SyncConfig> result;
        result = syncConfigRepository.getSyncConfigByEnabled(
                true,
                PageRequest.of(0, 1)
        );

        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());

    }

    @Test
    void find_sync_config_by_id() {
        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath("/source/test/1");
        initialSyncConfig.setTargetPath("/target/test/1");
        initialSyncConfig.setEnabled(Boolean.FALSE);

        SyncConfig needFoundSyncConfig = syncConfigRepository.saveAndFlush(initialSyncConfig);
        Short id = needFoundSyncConfig.getId();
        Optional<SyncConfig> foundSyncConfig = syncConfigRepository.findById(id);
        assertFalse(foundSyncConfig.orElseThrow().getEnabled());
    }

    @Test
    void when_save_source_path_and_target_path_exist_should_return_true() {
        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath("/source/test/");
        initialSyncConfig.setTargetPath("/target/test/");
        syncConfigRepository.saveAndFlush(initialSyncConfig);
        assertTrue(syncConfigRepository.existsSyncConfigBySourcePathAndTargetPath("/source/test/", "/target/test/"));
    }


}