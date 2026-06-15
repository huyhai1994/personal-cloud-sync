package org.mini_lab.personal_cloud_sync.repositories;

import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncAttemptRepository extends JpaRepository<SyncAttempt, Integer> {

}
