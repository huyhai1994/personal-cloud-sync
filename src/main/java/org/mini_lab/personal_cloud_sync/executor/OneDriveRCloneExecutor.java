package org.mini_lab.personal_cloud_sync.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.builder.OneDrivePathResolver;
import org.mini_lab.personal_cloud_sync.builder.RCloneCommandBuilder;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneDriveRCloneExecutor implements IRCloneExecutor {
    private final RCloneCommandBuilder rcloneCommandBuilder;
    private final OneDrivePathResolver oneDrivePathResolver;
    private final RCloneCommandExecutor rCloneCommandExecutor;

    @Override
    public RCloneResult sync(String sourcePath, String targetPath) throws IOException, InterruptedException {
        targetPath = oneDrivePathResolver.normalizePath(targetPath);
        List<String> command = rcloneCommandBuilder.command(sourcePath, targetPath);
        return rCloneCommandExecutor.executeCommand(command);
    }
}
