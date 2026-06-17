package org.mini_lab.personal_cloud_sync.exception;

public class SyncJobAlreadyRunningException extends RuntimeException {
    public SyncJobAlreadyRunningException() {
        super("Sync config already has pending/running or submitted job");
    }
}
