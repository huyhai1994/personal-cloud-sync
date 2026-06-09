package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncJob;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncJobRepository extends JpaRepository<SyncJob, Integer> {

    List<SyncJob> getAllByFinalStatus(JobStatus jobStatus, Pageable pageable);

}
