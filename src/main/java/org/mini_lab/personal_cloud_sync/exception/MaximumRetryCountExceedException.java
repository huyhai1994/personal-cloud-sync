package org.mini_lab.personal_cloud_sync.exception;

public class MaximumRetryCountExceedException extends RuntimeException {

    public MaximumRetryCountExceedException() {
        super("Maximum Retry Count Exceed should less or equal 5");
    }
}
