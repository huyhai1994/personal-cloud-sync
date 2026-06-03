package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
    @Transactional(readOnly = true)
    List<SyncConfig> getSyncConfigByEnabled(Boolean enabled, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            value = """
                    UPDATE SyncConfig sc
                    SET sc.enabled = :enabledStatus
                    WHERE sc.id = :id
                    """
    )
    int updateEnabledStatus(
            @Param("id") Short id,
            @Param("enabledStatus") Boolean enabledStatus
    );
}
