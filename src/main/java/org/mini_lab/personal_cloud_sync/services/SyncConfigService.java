package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.InternalServerException;
import org.mini_lab.personal_cloud_sync.exception.LocalPathIsNotDirectory;
import org.mini_lab.personal_cloud_sync.exception.MaximumRetryCountExceedException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.util.PathValidationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncConfigService {

    public static final int MAXIMUM_RETRY_LIMIT = 5;

    private final SyncConfigRepository syncConfigRepository;

    @Transactional
    public Short createSyncConfig(CreateSyncConfigRequest request) {
        Byte maximumRetryCount = request.getMaxRetry();
        String sourcePath = request.getSourcePath();
        String targetPath = request.getTargetPath();

        if (maximumRetryCount != null && maximumRetryCount > MAXIMUM_RETRY_LIMIT) {
            throw new MaximumRetryCountExceedException();
        }

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

        if (syncConfigRepository.existsSyncConfigBySourcePathAndTargetPath(sourcePath, targetPath)) {
            throw new DuplicateSyncConfigException();
        }

        NextScheduledAtRequest nextScheduledAtRequest = NextScheduledAtRequest.builder().runTime(request.getRunTime()).scheduleInterval(request.getScheduleInterval()).scheduleType(request.getScheduleType()).build();

        Optional<OffsetDateTime> nextScheduledAt = NextScheduledAtCalculationStrategy.estimateNextScheduledAt(nextScheduledAtRequest);

        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath);
        syncConfig.setTargetPath(targetPath);
        nextScheduledAt.ifPresent(syncConfig::setNextScheduledAt);

        try {
            SyncConfig persistedSyncConfig = syncConfigRepository.save(syncConfig);
            return persistedSyncConfig.getId();

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate sync config detected while saving. sourcePath={}, targetPath={}", sourcePath, targetPath, e);
            throw new DuplicateSyncConfigException();

        } catch (TransactionTimedOutException | DataAccessException e) {
            log.error("Failed to create sync config due to persistence error. sourcePath={}, targetPath={}", sourcePath, targetPath, e);
            throw new InternalServerException();
        }
    }
}
