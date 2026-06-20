package org.mini_lab.personal_cloud_sync.repositories;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncConfigRepositoryTest extends AbstractIntegrationTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-19T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Autowired
    private SyncConfigRepository syncConfigRepository;

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

    @Test
    void getSyncConfigByIdAndEnabled_whenSyncConfigExist_shouldReturnMatchingSyncConfig() {
        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath("/source/test/");
        initialSyncConfig.setTargetPath("/target/test/");
        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(initialSyncConfig);
        Optional<SyncConfig> syncConfigByIdAndEnabled = syncConfigRepository.getSyncConfigByIdAndEnabled(persistedSyncConfig.getId(), Boolean.TRUE);
        assertEquals(Boolean.TRUE, syncConfigByIdAndEnabled.orElseThrow().getEnabled());
    }

    @Test
    void getSyncConfigByIdAndEnabled_whenSyncConfigNotExist_shouldReturnEmpty() {
        Optional<SyncConfig> syncConfigByIdAndEnabled = syncConfigRepository.getSyncConfigByIdAndEnabled((short) 100, Boolean.TRUE);
        assertEquals(Optional.empty(), syncConfigByIdAndEnabled);
    }

    @Test
    void findDueSyncConfigs_shouldReturnOnlyDueAndNonManualConfigs() {

        SyncConfig dueConfig = new SyncConfig();
        dueConfig.setSourcePath("/source/test1");
        dueConfig.setTargetPath("/target/test1");
        dueConfig.setScheduleType(ScheduleType.INTERVAL);
        dueConfig.setScheduleInterval((short) 30);
        dueConfig.setNextScheduledAt(
                OffsetDateTime.now(fixedClock).minusMinutes(10)
        );

        SyncConfig manualConfig = new SyncConfig();
        manualConfig.setSourcePath("/source/test2");
        manualConfig.setTargetPath("/target/test2");
        manualConfig.setScheduleType(ScheduleType.MANUAL);

        syncConfigRepository.save(dueConfig);
        syncConfigRepository.save(manualConfig);

        List<SyncConfig> result =
                syncConfigRepository.findDueSyncConfigs(
                        ScheduleType.MANUAL,
                        OffsetDateTime.now(fixedClock),
                        PageRequest.of(
                                0,
                                5,
                                Sort.by("nextScheduledAt").ascending()
                        )
                );

        assertThat(result.get(0).getScheduleType())
                .isEqualTo(ScheduleType.INTERVAL);
    }
}