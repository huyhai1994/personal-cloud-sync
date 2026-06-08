package org.mini_lab.personal_cloud_sync.services;

import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class SyncConfigMapper {

    private static final byte DEFAULT_MAX_RETRY = 3;
    private static final ScheduleType DEFAULT_SCHEDULE_TYPE = ScheduleType.MANUAL;

    public SyncConfig mapCreateSyncConfigRequestToSyncConfig(CreateSyncConfigRequest request) {

        String sourcePath = request.getSourcePath();
        String targetPath = request.getTargetPath();
        Byte maxRetry = request.getMaxRetry() == null ? DEFAULT_MAX_RETRY : request.getMaxRetry();
        ScheduleType scheduleType = request.getScheduleType() == null ? DEFAULT_SCHEDULE_TYPE : request.getScheduleType();

        NextScheduledAtRequest nextScheduledAtRequest = NextScheduledAtRequest
                .builder()
                .runTime(request.getRunTime())
                .scheduleInterval(request.getScheduleInterval())
                .scheduleType(scheduleType).build();


        Optional<OffsetDateTime> nextScheduledAt = NextScheduledAtCalculationStrategy.estimateNextScheduledAt(nextScheduledAtRequest);


        SyncConfig syncConfig = new SyncConfig();
        syncConfig.setSourcePath(sourcePath);
        syncConfig.setTargetPath(targetPath);
        syncConfig.setMaxRetry(maxRetry);
        nextScheduledAt.ifPresent(syncConfig::setNextScheduledAt);
        return syncConfig;
    }
}
