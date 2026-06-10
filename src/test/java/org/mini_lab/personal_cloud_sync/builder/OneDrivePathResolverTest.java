package org.mini_lab.personal_cloud_sync.builder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OneDrivePathResolverTest {

    private final OneDrivePathResolver oneDrivePathResolver = new OneDrivePathResolver();

    @Test
    void normalizePath_shouldReturnOneDriveAtStart() {
        assertEquals("onedrive:/target-path", oneDrivePathResolver.normalizePath("/target-path"));
        assertEquals("onedrive:target-path", oneDrivePathResolver.normalizePath("target-path"));
    }

}