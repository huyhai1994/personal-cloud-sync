package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class SyncConfigServiceTest {
    @TempDir
    Path tempDir;


    @Autowired
    private SyncConfigService syncConfigService;

    @Test
    void maximum_retry_count_exceed_should_throw_exception() {
        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        createSyncConfigRequest.setMaxRetry((byte) 10);
        assertThrows(MaximumRetryCountExceedException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
    }

    @Test
    void source_or_target_path_is_null_should_throw_exception() {
        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        assertThrows(InvalidPathException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
        createSyncConfigRequest.setSourcePath("abc");
        assertThrows(InvalidPathException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
    }

    @Test
    void source_or_target_path_is_blank_should_throw_exception() {
        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        createSyncConfigRequest.setSourcePath(" ");
        createSyncConfigRequest.setTargetPath(" ");
        assertThrows(InvalidPathException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
        assertThrows(InvalidPathException.class, () -> syncConfigService.createSyncConfig(createSyncConfigRequest));
    }

    @Test
    void local_path_is_not_directory_should_throw_exception() throws IOException {
        Path sourceFile = Files.createFile(tempDir.resolve("source.txt"));

        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        createSyncConfigRequest.setSourcePath(sourceFile.toString());
        createSyncConfigRequest.setTargetPath(tempDir.toString());

        assertThrows(LocalPathIsNotDirectory.class, () ->
                syncConfigService.createSyncConfig(createSyncConfigRequest)
        );
    }

    @Test
    void local_path_is_not_exist_should_throw_exception() {
        Path notExistPath = tempDir.resolve("not-exist-dir");

        CreateSyncConfigRequest createSyncConfigRequest = new CreateSyncConfigRequest();
        createSyncConfigRequest.setSourcePath(notExistPath.toString());
        createSyncConfigRequest.setTargetPath(tempDir.toString());

        assertThrows(InvalidPathException.class, () ->
                syncConfigService.createSyncConfig(createSyncConfigRequest)
        );
    }
}