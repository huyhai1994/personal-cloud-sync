package org.mini_lab.personal_cloud_sync.repositories;

import jakarta.persistence.LockModeType;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SyncConfigRepository extends JpaRepository<SyncConfig, Short> {
    @Transactional(readOnly = true)
    @Deprecated
    List<SyncConfig> getSyncConfigByEnabled(Boolean enabled, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SyncConfig> getSyncConfigByIdAndEnabled(Short id, Boolean enabled);

    boolean existsSyncConfigBySourcePathAndTargetPath(String sourcePath, String targetPath);

    boolean existsSyncConfigBySourcePathAndTargetPathAndScheduleType(String sourcePath, String targetPath, ScheduleType scheduleType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select sc
            from SyncConfig sc
            where sc.enabled = true
              and sc.scheduleType <> :scheduleType
              and sc.nextScheduledAt <= :now
            """)
    List<SyncConfig> findDueSyncConfigs(
            @Param("scheduleType") ScheduleType scheduleType,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
