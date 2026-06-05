package org.mini_lab.personal_cloud_sync.exception;

public class DuplicateSyncConfigException extends RuntimeException {

    public DuplicateSyncConfigException() {
        super("Sync config already exists!");
    }
}
