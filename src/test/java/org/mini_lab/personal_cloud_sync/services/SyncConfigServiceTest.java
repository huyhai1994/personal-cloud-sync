package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncConfigServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private SyncConfigRepository syncConfigRepository;

    @InjectMocks
    private SyncConfigService syncConfigService;

    @Test
    void maximum_retry_count_exceed_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setMaxRetry((byte) 10);

        // when & then
        assertThrows(MaximumRetryCountExceedException.class, () ->
                syncConfigService.createSyncConfig(request)
        );
    }

    @Test
    void source_path_is_null_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );
    }

    @Test
    void target_path_is_null_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
        );
    }

    @Test
    void source_path_is_blank_should_throw_exception() {
        // given
        CreateSyncConfigRequest request = new CreateSyncConfigRequest();
        request.setSourcePath(" ");
        request.setTargetPath(tempDir.toString());

        // when & then
        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(request)
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
                syncConfigService.createSyncConfig(request)
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
                syncConfigService.createSyncConfig(request)
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
                syncConfigService.createSyncConfig(request)
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
                syncConfigService.createSyncConfig(request)
        );

        verify(syncConfigRepository).existsSyncConfigBySourcePathAndTargetPath(
                sourcePath.toString(),
                targetPath.toString()
        );

        verify(syncConfigRepository, never()).save(any());
    }
}