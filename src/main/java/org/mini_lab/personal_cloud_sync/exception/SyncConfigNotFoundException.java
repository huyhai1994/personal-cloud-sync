package org.mini_lab.personal_cloud_sync.exception;

public class SyncConfigNotFoundException extends RuntimeException {

    public SyncConfigNotFoundException() {
        super("Sync Config not found!");
    }
}
