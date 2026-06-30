package org.mini_lab.personal_cloud_sync.aspect;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.dto.ErrorDetail;
import org.mini_lab.personal_cloud_sync.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionControllerAdviceTest {

    private final ExceptionControllerAdvice handler =
            new ExceptionControllerAdvice();

    @Test
    void handle_maximum_retry_count_exceed_exception_should_return_400() {
        ResponseEntity<ErrorDetail> response =
                handler.handleBadRequest(new MaximumRetryCountExceedException());

        assertBadRequest(response, "Maximum Retry Count Exceed should less or equal 5");
    }

    @Test
    void handle_local_path_not_directory_should_return_400() {
        ResponseEntity<ErrorDetail> response =
                handler.handleBadRequest(new LocalPathIsNotDirectory());

        assertBadRequest(response, "Local Path is not Directory");
    }

    @Test
    void handle_duplicate_sync_config_should_return_400() {
        ResponseEntity<ErrorDetail> response =
                handler.handleBadRequest(new DuplicateSyncConfigException());

        assertBadRequest(response, "Sync config already exists");
    }

    @Test
    void handle_illegal_argument_exception_should_return_400() {
        ResponseEntity<ErrorDetail> response =
                handler.handleBadRequest(
                        new IllegalArgumentException(
                                "scheduleInterval must be greater than 0"
                        )
                );

        assertBadRequest(response, "scheduleInterval must be greater than 0");
    }

    @Test
    void handle_invalid_path_exception_should_return_400() {
        ResponseEntity<ErrorDetail> response =
                handler.handleInvalidPathException();

        assertBadRequest(response, "Source/Target Path not valid");
    }

    @Test
    void handle_conflict_should_return_409(){
        SyncJobAlreadyActiveException syncJobAlreadyActiveException = new SyncJobAlreadyActiveException();
        ResponseEntity<ErrorDetail> response =
                handler.handleConflict(syncJobAlreadyActiveException);

        assertEquals("Sync config already has pending/running or submitted job", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handle_internal_server_error_exception_should_return_500() {
        InternalServerException ex = new InternalServerException();
        ResponseEntity<ErrorDetail> response =
                handler.handleInternalServerException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody().getMessage());
    }

    @Test
    void handle_config_not_found_exception_should_return_404() {
        SyncConfigNotFoundException syncConfigNotFoundException = new SyncConfigNotFoundException();
        ResponseEntity<ErrorDetail> response =
                handler.handleNotFoundRequest(syncConfigNotFoundException);
        assertEquals("Sync Config not found!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private void assertBadRequest(
            ResponseEntity<ErrorDetail> response,
            String expectedMessage
    ) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }
}