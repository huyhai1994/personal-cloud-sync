package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;

public interface ISyncConfigValidator {
    void validateCreateSyncConfigRequest(CreateSyncConfigRequest request);
    void validateSourcePath(String sourcePath);

    void validateTargetPath(String targetPath);
}
