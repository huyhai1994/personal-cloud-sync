package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
    @Transactional(readOnly = true)
    List<SyncConfig> getSyncConfigByEnabled(Boolean enabled, Pageable pageable);

    boolean existsSyncConfigBySourcePathAndTargetPath(String sourcePath, String targetPath);

}
