package org.mini_lab.personal_cloud_sync.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathValidationUtilsTest {

    @TempDir
    Path testPath;

    private String existLocalFilePath;

    @BeforeEach
    void setUp() {
        existLocalFilePath = testPath.toString();
    }

    @Test
    void if_path_not_exist_return_false() {
        String NON_EXIST_PATH = "abc<>xyz";
        assertFalse(PathValidationUtils.pathIsExits(NON_EXIST_PATH));
    }

    @Test
    void if_path_exist_return_true() {
        assertTrue(PathValidationUtils.pathIsExits(existLocalFilePath));
    }

    @Test
    void check_path_is_directory() {
        assertTrue(PathValidationUtils.pathIsDirectory(existLocalFilePath));
    }

    @Test
    void check_if_two_path_is_same() {
        assertTrue(PathValidationUtils.isTwoPathSame("samepath", "samepath"));
    }


}