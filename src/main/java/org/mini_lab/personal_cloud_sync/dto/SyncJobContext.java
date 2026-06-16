package org.mini_lab.personal_cloud_sync.dto;

import lombok.Builder;

@Builder
public record SyncJobContext(Integer syncJobId, Integer syncAttemptId, String sourcePath, String targetPath) {
}
