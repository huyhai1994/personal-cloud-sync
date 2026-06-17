package org.mini_lab.personal_cloud_sync.dto;

import org.mini_lab.personal_cloud_sync.enums.JobStatus;

public record SyncJobResponse(Integer syncJobId, JobStatus jobStatus) {
}
