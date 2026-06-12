package org.mini_lab.personal_cloud_sync.exception;

public class InvalidJobStateTransitionException extends RuntimeException {
    public InvalidJobStateTransitionException() {
        super("Invalid Job State Transition");
    }
}
