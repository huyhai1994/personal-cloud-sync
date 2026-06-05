package org.mini_lab.personal_cloud_sync.exception;

public class LocalPathIsNotDirectory extends RuntimeException {

    public LocalPathIsNotDirectory() {
        super("Local Path is not Directory!");
    }
}
