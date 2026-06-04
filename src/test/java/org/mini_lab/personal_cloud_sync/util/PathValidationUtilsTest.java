package org.mini_lab.personal_cloud_sync.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathValidationUtilsTest {

    private final String existLocalFilePath = System.getProperty("user.home") + "/workspace/backend-notes/project-manager-workflow/";
    private String cloudFilePath = System.getProperty("user.home") + "/OneDrive";


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
        assertTrue(PathValidationUtils.pathIsDirectory((cloudFilePath)));
    }

    @Test
    void check_if_two_path_is_same() {
        assertTrue(PathValidationUtils.isTwoPathSame("samepath", "samepath"));
    }


    @Test
    void should_return_true_when_mount_output_contains_mount_point() {
        String output = "onedrive: on /home/huyhai1994/OneDrive type fuse.rclone (rw,nosuid,nodev)\n";

        assertTrue(PathValidationUtils.isMountPointOutputContainsMountPoint(
                output,
                "/home/huyhai1994/OneDrive"
        ));
    }

    @Test
    void should_return_false_when_mount_output_does_not_contain_mount_point() {
        String output = "/dev/sda1 on / type ext4 (rw,relatime)\n";

        assertFalse(PathValidationUtils.isMountPointOutputContainsMountPoint(
                output,
                "/home/huyhai1994/OneDrive"
        ));
    }

}