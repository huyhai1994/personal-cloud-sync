package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.dto.RCloneResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RCloneCommandExecutor {
    public RCloneResult executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process ps = pb.start();
        InputStream inputStream = ps.getInputStream();
        String output;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            output = br.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = ps.waitFor();

        return RCloneResult
                .builder()
                .exitCode(exitCode)
                .errorMessage(output)
                .build();
    }
}
