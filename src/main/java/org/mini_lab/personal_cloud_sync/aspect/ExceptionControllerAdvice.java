package org.mini_lab.personal_cloud_sync.aspect;

import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.ErrorDetail;
import org.mini_lab.personal_cloud_sync.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.InvalidPathException;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler({
            MaximumRetryCountExceedException.class,
            LocalPathIsNotDirectory.class,
            DuplicateSyncConfigException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorDetail> handleBadRequest(Exception ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(SyncJobAlreadyActiveException.class)
    public ResponseEntity<ErrorDetail> handleConflict(Exception ex) {

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetail);
    }

    @ExceptionHandler(SyncConfigNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleNotFoundRequest(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());
        log.warn("RESPONSE_RETURNED httpStatus={} message={}", HttpStatus.NOT_FOUND, errorDetail.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetail);
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorDetail> handleInvalidPathException() {
        return badRequest("Source/Target Path not valid");
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDetail> handleInternalServerException(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetail);
    }

    private ResponseEntity<ErrorDetail> badRequest(String message) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(message);

        return ResponseEntity
                .badRequest()
                .body(errorDetail);
    }
}
