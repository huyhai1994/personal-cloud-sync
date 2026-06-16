package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
    @Transactional(readOnly = true)
    @Deprecated
    List<SyncConfig> getSyncConfigByEnabled(Boolean enabled, Pageable pageable);

    Optional<SyncConfig> getSyncConfigByIdAndEnabled(Short id, Boolean enabled);

    boolean existsSyncConfigBySourcePathAndTargetPath(String sourcePath, String targetPath);

}
