package org.mini_lab.personal_cloud_sync.util;

import io.micrometer.common.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class PathValidationUtils {
    private PathValidationUtils() {
    }


    public static boolean pathIsExits(String path) {
        return Files.exists(Paths.get(path));
    }

    public static boolean pathIsDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    public static boolean isMountPointOutputContainsMountPoint(String output, String mountPoint) {
        return output.lines().anyMatch(line -> line.contains(mountPoint));
    }

    public static boolean isTwoPathSame(String path1, String path2) {
        return path1.equals(path2);
    }

    public static boolean isPathNull(String path) {
        return Objects.isNull(path);
    }

    public static boolean isPathBlank(String path) {
        return StringUtils.isEmpty(path);

    }
}
