package org.mini_lab.personal_cloud_sync.repositories;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncConfigRepositoryTest extends AbstractIntegrationTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-19T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    private String source(String name) {
        return sourcePath.resolve(name).toString();
    }

    private String target(String name) {
        return targetPath.resolve(name).toString();
    }

    @Test
    void persist_sync_config_should_success() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(source("persist-source"));
        syncConfig.setTargetPath(target("persist-target"));

        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(syncConfig);

        assertNotNull(persistedSyncConfig.getId());
    }

    @Test
    void persist_invalid_sync_config_should_throw_exception() {
        SyncConfig syncConfig = new SyncConfig();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> syncConfigRepository.saveAndFlush(syncConfig)
        );
    }

    @Test
    void persist_sync_config_created_at_auto_generated() {
        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(source("created-at-source"));
        syncConfig.setTargetPath(target("created-at-target"));

        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(syncConfig);

        assertNotNull(persistedSyncConfig.getCreatedAt());
    }

    @Test
    void source_path_and_target_path_should_be_unique() {
        String duplicatedSourcePath = source("duplicated-source");
        String duplicatedTargetPath = target("duplicated-target");

        SyncConfig enabledConfig = new SyncConfig();
        enabledConfig.setSourcePath(duplicatedSourcePath);
        enabledConfig.setTargetPath(duplicatedTargetPath);

        SyncConfig disabledConfig = new SyncConfig();
        disabledConfig.setEnabled(false);
        disabledConfig.setSourcePath(duplicatedSourcePath);
        disabledConfig.setTargetPath(duplicatedTargetPath);

        syncConfigRepository.saveAndFlush(enabledConfig);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> syncConfigRepository.saveAndFlush(disabledConfig)
        );
    }

    @Test
    void get_sync_config_by_enabled_should_return_only_enabled_configs() {
        SyncConfig enabledConfig = new SyncConfig();
        enabledConfig.setSourcePath(source("enabled-source"));
        enabledConfig.setTargetPath(target("enabled-target"));

        SyncConfig disabledConfig = new SyncConfig();
        disabledConfig.setEnabled(false);
        disabledConfig.setSourcePath(source("disabled-source"));
        disabledConfig.setTargetPath(target("disabled-target"));

        syncConfigRepository.saveAndFlush(enabledConfig);
        syncConfigRepository.saveAndFlush(disabledConfig);

        List<SyncConfig> result = syncConfigRepository.getSyncConfigByEnabled(
                true,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());
    }

    @Test
    void get_sync_config_by_enabled_should_apply_pagination() {
        SyncConfig enabledConfig1 = new SyncConfig();
        enabledConfig1.setSourcePath(source("enabled-source-1"));
        enabledConfig1.setTargetPath(target("enabled-target-1"));

        SyncConfig enabledConfig2 = new SyncConfig();
        enabledConfig2.setSourcePath(source("enabled-source-2"));
        enabledConfig2.setTargetPath(target("enabled-target-2"));

        syncConfigRepository.saveAndFlush(enabledConfig1);
        syncConfigRepository.saveAndFlush(enabledConfig2);

        List<SyncConfig> result = syncConfigRepository.getSyncConfigByEnabled(
                true,
                PageRequest.of(0, 1)
        );

        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());
    }

    @Test
    void find_sync_config_by_id() {
        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath(source("find-by-id-source"));
        initialSyncConfig.setTargetPath(target("find-by-id-target"));
        initialSyncConfig.setEnabled(Boolean.FALSE);

        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(initialSyncConfig);

        Optional<SyncConfig> foundSyncConfig = syncConfigRepository.findById(
                persistedSyncConfig.getId()
        );

        assertFalse(foundSyncConfig.orElseThrow().getEnabled());
    }

    @Test
    void when_save_source_path_and_target_path_exist_should_return_true() {
        String existingSourcePath = source("existing-source");
        String existingTargetPath = target("existing-target");

        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath(existingSourcePath);
        initialSyncConfig.setTargetPath(existingTargetPath);

        syncConfigRepository.saveAndFlush(initialSyncConfig);

        assertTrue(
                syncConfigRepository.existsSyncConfigBySourcePathAndTargetPath(
                        existingSourcePath,
                        existingTargetPath
                )
        );
    }

    @Test
    void getSyncConfigByIdAndEnabled_whenSyncConfigExist_shouldReturnMatchingSyncConfig() {
        SyncConfig initialSyncConfig = new SyncConfig();
        initialSyncConfig.setSourcePath(source("get-by-id-enabled-source"));
        initialSyncConfig.setTargetPath(target("get-by-id-enabled-target"));

        SyncConfig persistedSyncConfig = syncConfigRepository.saveAndFlush(initialSyncConfig);

        Optional<SyncConfig> syncConfigByIdAndEnabled =
                syncConfigRepository.getSyncConfigByIdAndEnabled(
                        persistedSyncConfig.getId(),
                        Boolean.TRUE
                );

        assertEquals(Boolean.TRUE, syncConfigByIdAndEnabled.orElseThrow().getEnabled());
    }

    @Test
    void getSyncConfigByIdAndEnabled_whenSyncConfigNotExist_shouldReturnEmpty() {
        Optional<SyncConfig> syncConfigByIdAndEnabled =
                syncConfigRepository.getSyncConfigByIdAndEnabled(
                        (short) 100,
                        Boolean.TRUE
                );

        assertEquals(Optional.empty(), syncConfigByIdAndEnabled);
    }

    @Test
    void findDueSyncConfigs_shouldReturnOnlyDueAndNonManualConfigs() {
        SyncConfig dueConfig = new SyncConfig();
        dueConfig.setSourcePath(source("due-source"));
        dueConfig.setTargetPath(target("due-target"));
        dueConfig.setScheduleType(ScheduleType.INTERVAL);
        dueConfig.setScheduleInterval((short) 30);
        dueConfig.setNextScheduledAt(
                OffsetDateTime.now(fixedClock).minusMinutes(10)
        );

        SyncConfig manualConfig = new SyncConfig();
        manualConfig.setSourcePath(source("manual-source"));
        manualConfig.setTargetPath(target("manual-target"));
        manualConfig.setScheduleType(ScheduleType.MANUAL);

        syncConfigRepository.saveAndFlush(dueConfig);
        syncConfigRepository.saveAndFlush(manualConfig);

        List<SyncConfig> result =
                syncConfigRepository.findDueNonManualScheduleTypeSyncConfigs(
                        ScheduleType.MANUAL,
                        OffsetDateTime.now(fixedClock),
                        PageRequest.of(
                                0,
                                5,
                                Sort.by("nextScheduledAt").ascending()
                        )
                );

        assertEquals(1, result.size());
        assertThat(result.get(0).getScheduleType())
                .isEqualTo(ScheduleType.INTERVAL);
    }
}