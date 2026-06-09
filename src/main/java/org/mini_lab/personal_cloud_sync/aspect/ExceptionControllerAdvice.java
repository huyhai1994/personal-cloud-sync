package org.mini_lab.personal_cloud_sync.aspect;

import org.apache.coyote.Response;
import org.mini_lab.personal_cloud_sync.dto.ErrorDetail;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.InternalServerException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.InvalidPathException;

@RestControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler({
            MaximumRetryCountExceedException.class,
            LocalPathIsNotDirectory.class,
            DuplicateSyncConfigException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorDetail> handleBadRequest(Exception ex) {

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());

        return ResponseEntity.badRequest().body(errorDetail);
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ErrorDetail> handleInvalidPathException() {
        return badRequest("Source/Target Path not valid");
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDetail> handleInternalServerException(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());
        return ResponseEntity.status(500).body(errorDetail);
    }

    private ResponseEntity<ErrorDetail> badRequest(String message) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(message);

        return ResponseEntity
                .badRequest()
                .body(errorDetail);
    }
}
