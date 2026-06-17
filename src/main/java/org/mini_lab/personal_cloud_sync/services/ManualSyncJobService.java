package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.dto.SyncJobResponse;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManualSyncJobService {
    private final SyncJobDispatcher syncJobDispatcher;
    private final SyncJobCreationService syncJobCreationService;

    public SyncJobResponse createAndDispatch(Short syncConfigId) {
        SyncJob syncJob = syncJobCreationService.createPendingJob(syncConfigId);
        syncJobDispatcher.dispatch(syncJob.getId());
        return new SyncJobResponse(syncJob.getId(), syncJob.getFinalStatus());
    }
}
