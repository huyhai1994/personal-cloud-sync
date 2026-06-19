package org.mini_lab.personal_cloud_sync.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.mini_lab.personal_cloud_sync.exception.SyncJobAlreadyRunningException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.util.PathValidationUtils;
import org.springframework.stereotype.Component;

import java.nio.file.InvalidPathException;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncConfigValidator implements ISyncConfigValidator {
    public static final int MAXIMUM_RETRY_LIMIT = 5;
    private final SyncConfigRepository syncConfigRepository;

    public void validateCreateSyncConfigRequest(CreateSyncConfigRequest request) {
        ScheduleType scheduleType = request.getScheduleType();
        Short scheduleInterval = request.getScheduleInterval();
        LocalTime runTime = request.getRunTime();
        String sourcePath = request.getSourcePath();
        String targetPath = request.getTargetPath();
        Byte maximumRetryCount = request.getMaxRetry();

        validateMaximumRetryCount(maximumRetryCount);
        validatePath(sourcePath, targetPath);
        validateIfSourceAndTargetPathAndScheduleTypeExisted(sourcePath, targetPath, scheduleType);
        validateScheduleType(scheduleType, runTime, scheduleInterval);

    }

    @Override
    public void validateSourcePath(String sourcePath) {
        if (PathValidationUtils.isPathNull(sourcePath)) {
            throw new InvalidPathException("Source Path", "Source Path should not be null");
        }
        if (PathValidationUtils.isPathBlank(sourcePath)) {
            throw new InvalidPathException("Source Path", "Source Path should not be blank");
        }
        if (!PathValidationUtils.pathIsExits(sourcePath)) {
            throw new InvalidPathException("Source Path", "Local path does not exist");
        }
        if (!PathValidationUtils.pathIsDirectory(sourcePath)) {
            throw new LocalPathIsNotDirectory();
        }
    }

    @Override
    public void validateTargetPath(String targetPath) {
        if (PathValidationUtils.isPathNull(targetPath)) {
            throw new InvalidPathException("Target Path", "Target Path should not be null");
        }
        if (PathValidationUtils.isPathBlank(targetPath)) {
            throw new InvalidPathException("Target Path", "Target Path should not be blank");
        }
    }

    private static void validateScheduleType(ScheduleType scheduleType, LocalTime runTime, Short scheduleInterval) {
        if (scheduleType == ScheduleType.INTERVAL) {
            if (runTime != null) {
                throw new IllegalArgumentException("INTERVAL schedule must not have runTime");
            }

            if (scheduleInterval == null) {
                throw new IllegalArgumentException("INTERVAL schedule requires scheduleInterval");
            }

            if (scheduleInterval <= 0) {
                throw new IllegalArgumentException("scheduleInterval must be greater than 0");
            }
        } else if (scheduleType == ScheduleType.DAILY) {
            if (runTime == null) {
                throw new IllegalArgumentException("Daily schedule requires runTime");
            }

            if (scheduleInterval != null) {
                throw new IllegalArgumentException("Daily schedule must not have scheduleInterval");
            }
        }
    }

    private void validateIfSourceAndTargetPathAndScheduleTypeExisted(String sourcePath, String targetPath, ScheduleType scheduleType) {
        log.info("CHECK_DUPLICATE sourcePath={}, targetPath={}, scheduleType={}",
                sourcePath, targetPath, scheduleType);
        log.info("CHECK_DUPLICATE sourcePath={}, targetPath={}, scheduleType={}",
                sourcePath, targetPath, scheduleType);

        boolean exists = syncConfigRepository
                .existsSyncConfigBySourcePathAndTargetPathAndScheduleType(
                        sourcePath, targetPath, scheduleType
                );

        log.info("CHECK_DUPLICATE_RESULT exists={}", exists);
        if (Boolean.TRUE.equals(exists)) throw new DuplicateSyncConfigException();
    }

    private static void validatePath(String sourcePath, String targetPath) {
        if (PathValidationUtils.isPathNull(sourcePath)) {
            throw new InvalidPathException("Source Path", "Source Path should not be null");
        }

        if (PathValidationUtils.isPathNull(targetPath)) {
            throw new InvalidPathException("Target Path", "Target Path should not be null");
        }

        if (PathValidationUtils.isPathBlank(sourcePath)) {
            throw new InvalidPathException("Source Path", "Source Path should not be blank");
        }
        if (PathValidationUtils.isPathBlank(targetPath)) {
            throw new InvalidPathException("Target Path", "Target Path should not be blank");
        }

        if (!PathValidationUtils.pathIsExits(sourcePath)) {
            throw new InvalidPathException("Source Path", "Local path does not exist");
        }

        if (!PathValidationUtils.pathIsDirectory(sourcePath)) {
            throw new LocalPathIsNotDirectory();
        }
    }

    private static void validateMaximumRetryCount(Byte maximumRetryCount) {
        if (maximumRetryCount != null && maximumRetryCount > MAXIMUM_RETRY_LIMIT) {
            throw new MaximumRetryCountExceedException();
        }
    }
}
