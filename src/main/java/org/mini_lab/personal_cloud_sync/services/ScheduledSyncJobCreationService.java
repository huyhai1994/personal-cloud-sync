package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduledSyncJobCreationService {

    public static final List<JobStatus> ACTIVE_JOB_STATUS = List.of(JobStatus.PENDING, JobStatus.RUNNING, JobStatus.SUBMITTED);
    private final SyncJobRepository syncJobRepository;

    @Transactional
    public Optional<SyncJob> createPendingJob(SyncConfig syncConfig) {

        if (syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(syncConfig.getId(), ACTIVE_JOB_STATUS)) {
            log.info("ACTIVE_JOB_ALREADY_EXISTS_OR_STUCK syncConfigId={}", syncConfig.getId());
            return Optional.empty();
        }

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);

        return Optional.of(syncJobRepository.save(syncJob));
    }

}
