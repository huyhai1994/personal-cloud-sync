package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
    List<SyncConfig> getSyncConfigByEnabled(Boolean enabled, Pageable pageable);
}
