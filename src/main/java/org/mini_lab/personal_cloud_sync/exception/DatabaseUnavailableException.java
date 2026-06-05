package org.mini_lab.personal_cloud_sync.exception;

public class DatabaseUnavailableException extends RuntimeException {
    public DatabaseUnavailableException() {
        super("Database Unavailable");
    }
}
