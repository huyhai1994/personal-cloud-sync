package org.mini_lab.personal_cloud_sync.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException() {
        super("Unexpected server error!");
    }
}
