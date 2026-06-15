package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SyncAttemptRecorder {
    private final SyncAttemptRepository syncAttemptRepository;
    private final Clock systemClock;

    public Integer startAttempt(SyncJob syncJob) {
        SyncAttempt syncAttempt = new SyncAttempt();
        syncAttempt.setSyncJob(syncJob);
        syncAttempt.setAttemptStatus(JobStatus.RUNNING);
        syncAttempt.setStartAt(OffsetDateTime.now(systemClock));
        return syncAttemptRepository.save(syncAttempt).getId();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markSuccess(Integer attemptId) {
        SyncAttempt syncAttempt = syncAttemptRepository.findById(attemptId).orElseThrow();
        syncAttempt.setFinishedAt(OffsetDateTime.now(systemClock));
        syncAttempt.setAttemptStatus(JobStatus.SUCCESS);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markFailed(Integer attemptId) {
        SyncAttempt syncAttempt = syncAttemptRepository.findById(attemptId).orElseThrow();
        syncAttempt.setFinishedAt(OffsetDateTime.now(systemClock));
        syncAttempt.setAttemptStatus(JobStatus.FAILED);
    }


}
