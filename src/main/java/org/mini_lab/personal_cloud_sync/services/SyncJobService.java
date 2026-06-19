package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.SyncJobResponse;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobService {
    private final SyncJobDispatcher syncJobDispatcher;
    private final SyncJobCreationService syncJobCreationService;

    public SyncJobResponse createAndDispatch(Short syncConfigId) {
        SyncJob syncJob = syncJobCreationService.createPendingJob(syncConfigId);
        log.info("SYNC_JOB_CREATED syncJobId={} ", syncJob.getId());
        syncJobDispatcher.dispatch(syncJob.getId());
        return new SyncJobResponse(syncJob.getId(), syncJob.getFinalStatus());
    }
}
