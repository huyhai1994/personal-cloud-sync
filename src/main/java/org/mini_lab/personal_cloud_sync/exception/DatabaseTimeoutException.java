package org.mini_lab.personal_cloud_sync.exception;

public class DatabaseTimeoutException extends RuntimeException {

    public DatabaseTimeoutException() {
        super("Database Timeout!");
    }
}
