package org.mini_lab.personal_cloud_sync.repositories;

import jakarta.persistence.LockModeType;
import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT sj from SyncJob as sj
                    where sj.finalStatus = :jobStatus
                    and sj.createdAt < :currentTime - :timeOutLimit minute
            """
    )
    @EntityGraph(attributePaths = "syncAttempts")
    List<SyncJob> findTimedOutRunningJobs(
            @Param("jobStatus") JobStatus jobStatus,
            @Param("currentTime") OffsetDateTime currentTime,
            @Param("timeOutLimit") Integer timeOutLimit);

    List<SyncJob> getAllByFinalStatus(JobStatus jobStatus, Pageable pageable);

    @EntityGraph(attributePaths = "syncConfig")
    Optional<SyncJob> getSyncJobById(Integer id);

    @Query("""
            select
                case when count(sj) > 0then true else false end
            from SyncJob sj
            where sj.syncConfig.id = :syncConfigId
              and sj.finalStatus in :statuses
            """)
    boolean existsBySyncConfigIdAndFinalStatusIn(
            @Param("syncConfigId") Short syncConfigId,
            @Param("statuses") List<JobStatus> statuses
    );

    @Modifying
    @Query("""
            update SyncJob  j
            set j.finalStatus =:targetStatus
            where j.id = :syncJobId
            and j.finalStatus =:currentStatus
            """)
    int updateStatusIfCurrentStatus(
            Integer syncJobId,
            JobStatus currentStatus,
            JobStatus targetStatus
    );

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finishedAt =:finishedAt
                where sj.id = :id
            """)
    void updateUpdatedAtForTest(@Param("id") Integer id,
                                @Param("finishedAt") OffsetDateTime finishedAt);
}

