package org.mini_lab.personal_cloud_sync.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class OneDriveRCloneExecutor implements IRCloneExecutor {
    private final RCloneCommandBuilder rcloneCommandBuilder;
    private final OneDrivePathResolver oneDrivePathResolver;
    private final RCloneCommandExecutor rCloneCommandExecutor;

    @Override
    public RCloneResult sync(SyncJobContext syncJobContext) throws IOException, InterruptedException {
        String targetPath = syncJobContext.targetPath();
        String sourcePath = syncJobContext.sourcePath();
        targetPath = oneDrivePathResolver.normalizePath(targetPath);
        List<String> command = rcloneCommandBuilder.command(sourcePath, targetPath);
        log.info("RCLONE_STARTED  command={}", command);
        return rCloneCommandExecutor.executeCommand(command);
    }
}
