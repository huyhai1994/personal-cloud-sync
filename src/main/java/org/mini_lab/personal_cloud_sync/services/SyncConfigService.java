package org.mini_lab.personal_cloud_sync.services;

import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.mini_lab.personal_cloud_sync.util.PathValidationUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.util.stream.Collectors;

@Service
public class SyncConfigService {

    public static final int MAXIMUM_RETRY_LIMIT = 5;

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

    public Short createSyncConfig(CreateSyncConfigRequest request) {
        Byte maximumRetryCount = request.getMaxRetry();
        String sourcePath = request.getSourcePath();
        String targetPath = request.getTargetPath();

        if (maximumRetryCount != null && maximumRetryCount > MAXIMUM_RETRY_LIMIT)
            throw new MaximumRetryCountExceedException();
        if (PathValidationUtils.isPathNull(sourcePath))
            throw new InvalidPathException("Source Path", "Source Path Should not be null");
        if (PathValidationUtils.isPathNull(targetPath))
            throw new InvalidPathException("target Path", "Target Path Should not be null");
        if (PathValidationUtils.isPathBlank(sourcePath))
            throw new InvalidPathException("Source Path", "Source Path Should not be blank");
        if (PathValidationUtils.isPathBlank(targetPath))
            throw new InvalidPathException("target Path", "Target Path Should not be blank");

        return 0;
    }
}
