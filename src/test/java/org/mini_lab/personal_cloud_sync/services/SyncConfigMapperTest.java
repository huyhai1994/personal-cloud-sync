package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SyncConfigMapperTest {

    @InjectMocks
    SyncConfigMapper syncConfigMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-11T00:00:00Z"),
                ZoneOffset.UTC
        );

        syncConfigMapper = new SyncConfigMapper(fixedClock);
    }


    @Test
    void mapCreateSyncConfigRequestToSyncConfig_shouldReturnDefaultMaxRetry_whenMaxRetryIsNull() {
        CreateSyncConfigRequest createSyncConfigRequest = CreateSyncConfigRequest
                .builder()
                .sourcePath(tempDir.toString())
                .targetPath(tempDir.toString())
                .build();

        SyncConfig expectedSyncConfig = new SyncConfig();
        expectedSyncConfig.setSourcePath(tempDir.toString());
        expectedSyncConfig.setTargetPath(tempDir.toString());
        expectedSyncConfig.setScheduleType(ScheduleType.MANUAL);
        expectedSyncConfig.setMaxRetry((byte) 3);
        expectedSyncConfig.setEnabled(true);

        SyncConfig actualSyncConfig = syncConfigMapper.mapCreateSyncConfigRequestToSyncConfig(createSyncConfigRequest);

        assertEquals(expectedSyncConfig.getMaxRetry(), actualSyncConfig.getMaxRetry());
        assertEquals(expectedSyncConfig.getSourcePath(), actualSyncConfig.getSourcePath());
        assertEquals(expectedSyncConfig.getTargetPath(), actualSyncConfig.getTargetPath());
        assertEquals(expectedSyncConfig.getEnabled(), actualSyncConfig.getEnabled());
        assertEquals(expectedSyncConfig.getScheduleType(), actualSyncConfig.getScheduleType());

    }

    @Test
    void mapCreateSyncConfigRequestToSyncConfig_shouldReturnDefaultScheduleType_whenScheduleTypeIsNull() {
        CreateSyncConfigRequest createSyncConfigRequest = CreateSyncConfigRequest
                .builder()
                .sourcePath(tempDir.toString())
                .targetPath(tempDir.toString())
                .maxRetry((byte) 8)
                .build();

        SyncConfig expectedSyncConfig = new SyncConfig();
        expectedSyncConfig.setSourcePath(tempDir.toString());
        expectedSyncConfig.setTargetPath(tempDir.toString());
        expectedSyncConfig.setScheduleType(ScheduleType.MANUAL);
        expectedSyncConfig.setMaxRetry((byte) 8);
        expectedSyncConfig.setEnabled(true);

        SyncConfig actualSyncConfig = syncConfigMapper.mapCreateSyncConfigRequestToSyncConfig(createSyncConfigRequest);

        assertEquals(expectedSyncConfig.getMaxRetry(), actualSyncConfig.getMaxRetry());
        assertEquals(expectedSyncConfig.getSourcePath(), actualSyncConfig.getSourcePath());
        assertEquals(expectedSyncConfig.getTargetPath(), actualSyncConfig.getTargetPath());
        assertEquals(expectedSyncConfig.getEnabled(), actualSyncConfig.getEnabled());
        assertEquals(expectedSyncConfig.getScheduleType(), actualSyncConfig.getScheduleType());

    }
}