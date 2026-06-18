package org.mini_lab.personal_cloud_sync.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.configuration.RCloneProperties;
import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RCloneCommandExecutor {
    private final RCloneProperties rCloneProperties;

    public RCloneResult executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        boolean finished = process.waitFor(rCloneProperties.getTimeOutSecond(), TimeUnit.SECONDS);

        if (!finished) {
            process.destroy();

            if (!process.waitFor(rCloneProperties.getTimeOutSecond(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }

            return RCloneResult.builder()
                    .exitCode(-1)
                    .errorMessage("RClone process timed out")
                    .build();
        }

        InputStream inputStream = process.getInputStream();
        String output;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            output = br.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = process.exitValue();

        return RCloneResult.builder()
                .exitCode(exitCode)
                .errorMessage(output)
                .build();
    }
}
