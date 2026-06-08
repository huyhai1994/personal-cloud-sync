package org.mini_lab.personal_cloud_sync.services;

import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;

public interface ISyncConfigValidator {
    void validateCreateSyncConfigRequest(CreateSyncConfigRequest request);
}
