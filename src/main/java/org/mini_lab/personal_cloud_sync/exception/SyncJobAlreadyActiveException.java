package org.mini_lab.personal_cloud_sync.exception;

public class SyncJobAlreadyActiveException extends RuntimeException {
    public SyncJobAlreadyActiveException() {
        super("Sync config already has pending/running or submitted job");
    }
}
