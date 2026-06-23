package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.exception.SyncConfigNotFoundException;
import org.mini_lab.personal_cloud_sync.exception.SyncJobAlreadyActiveException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ManualSyncJobCreationService {

    private final SyncConfigRepository syncConfigRepository;
    private final SyncJobRepository syncJobRepository;

    @Transactional
    public SyncJob createPendingJob(Short syncConfigId) {
        SyncConfig syncConfig = syncConfigRepository.getSyncConfigByIdAndEnabled(syncConfigId, Boolean.TRUE).orElseThrow(SyncConfigNotFoundException::new);

        if (syncJobRepository.existsBySyncConfigIdAndFinalStatusIn(syncConfigId, List.of(JobStatus.PENDING, JobStatus.RUNNING, JobStatus.SUBMITTED))) {
            throw new SyncJobAlreadyActiveException();
        }

        SyncJob syncJob = new SyncJob();
        syncJob.setSyncConfig(syncConfig);
        syncJob.setFinalStatus(JobStatus.PENDING);

        return syncJobRepository.save(syncJob);
    }

}
