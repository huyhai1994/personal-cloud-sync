package org.mini_lab.personal_cloud_sync.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.component.RCloneCommandBuilder;
import org.mini_lab.personal_cloud_sync.configuration.RCloneProperties;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.component.RCloneCommandExecutor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RCloneCommandExecutorTest {

    @InjectMocks
    RCloneCommandExecutor rCloneCommandExecutor;

    @Mock
    RCloneCommandBuilder rCloneCommandBuilder;

    @Mock
    RCloneProperties rCloneProperties;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @Test
    void executeCommand_syncLocalFile_shouldSuccess() throws IOException, InterruptedException {

        when(rCloneCommandBuilder
                .command(sourcePath.toString(), targetPath.toString()))
                .thenReturn(List.of("rclone", "sync", sourcePath.toString(), targetPath.toString(), "--log-level", "INFO"));

        when(rCloneProperties.getTimeOutSecond()).thenReturn(5);

        Path sourceFile = sourcePath.resolve("test.txt");
        Path targetFile = targetPath.resolve("test.txt");
        Files.writeString(sourceFile, "Hello World!");
        RCloneResult result = rCloneCommandExecutor.executeCommand(rCloneCommandBuilder.command(sourcePath.toString(), targetPath.toString())
        );

        assertEquals(0, result.getExitCode());
        assertTrue(Files.exists(targetPath.resolve("test.txt")));
        assertEquals(Files.readString(sourceFile), Files.readString(targetFile));

    }

    @Test
    void executeCommand_whenCommandTimeout_shouldKillProcess()
            throws IOException, InterruptedException {

        when(rCloneProperties.getTimeOutSecond()).thenReturn(5);
        RCloneResult result = rCloneCommandExecutor.executeCommand(
                List.of("bash", "-c", "sleep 7")
        );

        assertEquals(-1, result.getExitCode());
        assertEquals("RClone process timed out", result.getErrorMessage());
    }
}