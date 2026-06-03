package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
}
