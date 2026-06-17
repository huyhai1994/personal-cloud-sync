package org.mini_lab.personal_cloud_sync.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.mini_lab.personal_cloud_sync.dto.SyncJobContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Profile("test")
@Component
@RequiredArgsConstructor
public class LocalRCloneExecutor implements IRCloneExecutor {
    private final RCloneCommandBuilder rcloneCommandBuilder;
    private final RCloneCommandExecutor rCloneCommandExecutor;

    @Override
    public RCloneResult sync(SyncJobContext syncJobContext) throws IOException, InterruptedException {
        String targetPath = syncJobContext.targetPath();
        String sourcePath = syncJobContext.sourcePath();
        List<String> command = rcloneCommandBuilder.command(sourcePath, targetPath);
        return rCloneCommandExecutor.executeCommand(command);
    }
}
