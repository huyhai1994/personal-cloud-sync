package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.component.ISyncConfigValidator;
import org.mini_lab.personal_cloud_sync.component.SyncConfigMapper;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.InternalServerException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncConfigServiceTest {

    @TempDir
    Path tempDir;

    @InjectMocks
    private SyncConfigService syncConfigService;


    @Mock
    private SyncConfigRepository syncConfigRepository;

    @Mock
    private ISyncConfigValidator syncConfigValidator;

    @Mock
    private SyncConfigMapper syncConfigMapper;

    @Test
    void maximum_retry_count_exceed_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();

        // when
        doThrow(new MaximumRetryCountExceedException())
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // then
        assertThrows(MaximumRetryCountExceedException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());

    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsInvalidPathException_sourcePathIsNull() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setTargetPath(tempDir.toString());

        doThrow(new InvalidPathException("sourcePath", "sourcePath should not be null"))
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsInvalidPathException_targetPathIsNull() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(tempDir.toString());

        // when & then
        doThrow(new InvalidPathException("targetPath", "targetPath should not be null"))
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsInvalidPathException_sourcePathIsBlank() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(" ");
        request.setTargetPath(tempDir.toString());

        // when & then
        doThrow(new InvalidPathException("sourcePath", "sourcePath should not be blank"))
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsInvalidPathException_targetPathIsBlank() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(tempDir.toString());
        request.setTargetPath(" ");

        doThrow(new InvalidPathException("targetPath", "targetPath should not be blank"))
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsLocalPathIsNotDirectory_sourceFileNotExist() throws IOException {
        // given
        Path sourceFile = Files.createFile(tempDir.resolve("source.txt"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourceFile.toString());
        request.setTargetPath(tempDir.toString());

        doThrow(new LocalPathIsNotDirectory())
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(LocalPathIsNotDirectory.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldNotSave_whenValidatorThrowsDuplicateSyncConfigException() throws IOException {
        // given
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        request.setScheduleType(ScheduleType.MANUAL);

        doThrow(new DuplicateSyncConfigException())
                .when(syncConfigValidator)
                .validateCreateSyncConfigRequest(request);

        // when & then
        assertThrows(DuplicateSyncConfigException.class, () ->
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigValidator).validateCreateSyncConfigRequest(request);
        verify(syncConfigRepository, never()).save(any());
    }

    @Test
    void createSyncConfig_shouldThrowInternalServerException_whenDataBaseConnectionFailed() throws IOException {
        Path sourcePath = Files.createDirectory(tempDir.resolve("source"));
        Path targetPath = Files.createDirectory(tempDir.resolve("target"));

        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(sourcePath.toString());
        request.setTargetPath(targetPath.toString());
        when(syncConfigRepository.save(any(SyncConfig.class))).thenThrow(new DataAccessException("DB is not reachable") {
        });
        when(syncConfigMapper.mapCreateSyncConfigRequestToSyncConfig(request)).thenReturn(new SyncConfig());

        assertThrows(InternalServerException.class, () -> syncConfigService.createSyncConfig(request));

        verify(syncConfigMapper).mapCreateSyncConfigRequestToSyncConfig(request);
        verify(syncConfigRepository).save(any(SyncConfig.class));
    }
}