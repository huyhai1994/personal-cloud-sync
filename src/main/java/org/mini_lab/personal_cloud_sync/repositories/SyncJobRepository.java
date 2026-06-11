package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Integer> {

    List<SyncJob> getAllByFinalStatus(JobStatus jobStatus, Pageable pageable);

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

}
