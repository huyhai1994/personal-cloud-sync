package org.mini_lab.personal_cloud_sync.repositories;

import io.micrometer.core.annotation.Timed;
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
                    and sj.heartBeatAt < :cutOffTime
            """
    )
    @EntityGraph(attributePaths = "syncAttempts")
    @Timed(
            value = "sync.job.repository.find_timed_out_running_jobs",
            description = "Time taken to find timed out running jobs"
    )
    List<SyncJob> findTimedOutRunningJobs(
            @Param("jobStatus") JobStatus jobStatus,
            @Param("cutOffTime") OffsetDateTime cutOffTime);

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
                set sj.heartBeatAt =:heartBeatAt
                where sj.id = :id
            """)
    void updateHeartBeatAtForTest(@Param("id") Integer id,
                                  @Param("heartBeatAt") OffsetDateTime heartBeatAt);

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finalStatus = :failed,
                    sj.finishedAt = :now
                where sj.id = :id
                  and sj.finalStatus = :running
            """)
    int markFailedIfRunning(@Param("id") Integer syncJobId,
                             @Param("failed") JobStatus failed,
                             @Param("running") JobStatus running,
                             @Param("now") OffsetDateTime now);

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finalStatus = :success,
                    sj.finishedAt = :now
                where sj.id = :id
                  and sj.finalStatus = :running
            """)
    int markSuccessIfRunning(@Param("id") Integer syncJobId,
                               @Param("success") JobStatus success,
                               @Param("running") JobStatus running,
                               @Param("now") OffsetDateTime now);

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finalStatus = :running,
                    sj.startAt = :now
                where sj.id = :id
                  and sj.finalStatus = :submitted
            """)
    int markRunningIfSubmitted(@Param("id") Integer syncJobId,
                               @Param("running") JobStatus running,
                               @Param("submitted") JobStatus submitted,
                               @Param("now") OffsetDateTime now);

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finalStatus = :submitted,
                    sj.submittedAt = :now
                where sj.id = :id
                  and sj.finalStatus = :pending
            """)
    int markSubmittedIfPending(@Param("id") Integer syncJobId,
                               @Param("pending") JobStatus pending,
                               @Param("submitted") JobStatus submitted,
                               @Param("now") OffsetDateTime now);

    @Modifying
    @Query("""
                update SyncJob sj
                set sj.finalStatus = :submitFailed,
                    sj.finishedAt = :now
                where sj.id = :id
                  and sj.finalStatus = :pending
            """)
    int markSubmitFailedIfPending(@Param("id") Integer syncJobId,
                                  @Param("submitFailed") JobStatus submitFailed,
                                  @Param("pending") JobStatus pending,
                                  @Param("now") OffsetDateTime now);

}

