package org.mini_lab.personal_cloud_sync.services;

import org.mini_lab.personal_cloud_sync.util.PathValidationUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class SyncConfigService {

    public boolean checkMountViaCommand(String mountPoint) throws IOException, InterruptedException {
        String command = System.getProperty("os.name").toLowerCase().contains("win") ? "mountvol" : "mount";
        ProcessBuilder pb = new ProcessBuilder(command);
        Process ps = pb.start();
        InputStream inputStream = ps.getInputStream();
        String output;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            output = br.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = ps.waitFor();
        if (0 != exitCode) {
            return false;
        }
        return PathValidationUtils.isMountPointOutputContainsMountPoint(output, mountPoint);
    }
}
