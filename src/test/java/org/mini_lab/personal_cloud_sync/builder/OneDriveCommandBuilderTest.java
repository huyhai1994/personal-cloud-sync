package org.mini_lab.personal_cloud_sync.builder;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class OneDriveCommandBuilderTest {
    private final OneDriveCommandBuilder oneDriveCommandBuilder = new OneDriveCommandBuilder();

    @Test
    void command_should_remove_slash_if_target_path_starts_with_slash() {
        List<String> actual = oneDriveCommandBuilder.command(
                "/source-path",
                "/target-path"
        );

        assertEquals(List.of(
                "rclone",
                "sync",
                "/source-path",
                "onedrive:target-path",
                "--verbose",
                "--log-level",
                "INFO"
        ), actual);
    }

    @Test
    void command_should_keep_target_path_if_target_path_does_not_start_with_slash() {
        List<String> actual = oneDriveCommandBuilder.command(
                "/source-path",
                "target-path"
        );

        assertEquals(List.of(
                "rclone",
                "sync",
                "/source-path",
                "onedrive:target-path",
                "--verbose",
                "--log-level",
                "INFO"
        ), actual);
    }
}