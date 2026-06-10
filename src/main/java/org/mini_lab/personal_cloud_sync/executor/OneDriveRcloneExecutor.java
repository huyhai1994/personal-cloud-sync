package org.mini_lab.personal_cloud_sync.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.builder.OneDriveCommandBuilder;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneDriveRcloneExecutor implements RCloneExecutor {
    private final OneDriveCommandBuilder oneDriveCommandBuilder;

    @Override
    public RCloneResult sync(String sourcePath, String targetPath) throws IOException, InterruptedException {
        List<String> command = oneDriveCommandBuilder.command(sourcePath, targetPath);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process ps = pb.start();
        InputStream inputStream = ps.getInputStream();
        String output;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            output = br.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = ps.waitFor();
        if (exitCode != 0) {
            log.error("Rclone sync failed with exit code: {} ", exitCode);
        }

        return RCloneResult
                .builder()
                .exitCode(exitCode)
                .errorMessage(output)
                .build();
    }
}
