package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncConfigValidatorTest {
    @TempDir
    Path tempDir;

    @InjectMocks
    private SyncConfigValidator syncConfigValidator;

    @Mock
    private SyncConfigRepository syncConfigRepository;

    @Test
    void maximum_retry_count_exceed_should_throw_exception() {
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setMaxRetry((byte) 10);

        assertThrows(MaximumRetryCountExceedException.class, () -> syncConfigValidator.validateCreateSyncConfigRequest(request));
    }

    @Test
    void source_path_is_null_should_throw_exception() {
        // given

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () -> syncConfigValidator.validateCreateSyncConfigRequest(request));
    }

    @Test
    void target_path_is_null_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () -> syncConfigValidator.validateCreateSyncConfigRequest(request));
    }

    @Test
    void source_path_is_blank_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(" ");
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigValidator.validateCreateSyncConfigRequest(request)
        );
    }

    @Test
    void target_path_is_blank_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(tempDir.toString());
        request.setTargetPath(" ");

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigValidator.validateCreateSyncConfigRequest(request)
        );
    }

    @Test
    void local_path_is_not_directory_should_throw_exception() throws IOException {
        // given
        Path sourceFile = Files.createFile(tempDir.resolve("source.txt"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourceFile.toString());
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(LocalPathIsNotDirectory.class, () ->
                syncConfigValidator.validateCreateSyncConfigRequest(request)
        );
    }

    @Test
    void local_path_is_not_exist_should_throw_exception() {
        // given
        Path notExistPath = tempDir.resolve("not-exist-dir");

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(notExistPath.toString());
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigValidator.validateCreateSyncConfigRequest(request)
        );
    }

    @Test
    void sync_config_already_exists_should_throw_exception() throws IOException {
        // given
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());

        when(syncConfigRepository.existsSyncConfigBySourcePathAndTargetPath(
                sourcePath.toString(),
                targetPath.toString()
        )).thenReturn(true); //Nó giả lập repo trả lời rằng config đã tồn tại.

        // when & then
        assertThrows(DuplicateSyncConfigException.class, () ->
                syncConfigValidator.validateCreateSyncConfigRequest(request)
        );

    }

    @Test
    void scheduleType_interval_shouldThrowIllegalArgumentException_whenRuntimeExist() throws IOException {
        // given
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setRunTime(LocalTime.parse("10:00"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> syncConfigValidator.validateCreateSyncConfigRequest(request)
        );

        assertEquals("INTERVAL schedule must not have runTime", exception.getMessage());


    }

    @Test
    void scheduleType_interval_shouldThrowIllegalArgumentException_whenScheduleIntervalIsNull() throws IOException {
        // given
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> syncConfigValidator.validateCreateSyncConfigRequest(request)
        );

        assertEquals("INTERVAL schedule requires scheduleInterval", exception.getMessage());


    }

    @Test
    void scheduleType_daily_shouldThrowIllegalArgumentException_whenRuntimeIsNull() throws IOException {

        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.DAILY);
        request.setRunTime(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> syncConfigValidator.validateCreateSyncConfigRequest(request));
        assertEquals("Daily schedule requires runTime", exception.getMessage());

    }

    @Test
    void scheduleType_daily_shouldThrowException_whenScheduleIntervalIsNotNull() throws IOException {
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.DAILY);
        request.setScheduleInterval((short) 10);
        request.setRunTime(LocalTime.parse("10:00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> syncConfigValidator.validateCreateSyncConfigRequest(request));
        assertEquals("Daily schedule must not have scheduleInterval", exception.getMessage());

    }

    @Test
    void scheduleType_interval_shouldThrowException_whenScheduleIntervalMustBeGreaterThan0() throws IOException {

        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval((short) 0);
        request.setRunTime(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> syncConfigValidator.validateCreateSyncConfigRequest(request)
        );

        assertEquals("scheduleInterval must be greater than 0", exception.getMessage());
    }
}