package org.mini_lab.personal_cloud_sync.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "sync_job")
public class SyncJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_config_id", referencedColumnName = "id")
    private SyncConfig syncConfig;

    @OneToMany(mappedBy = "syncJob", fetch = FetchType.LAZY)
    private List<SyncAttempt> syncAttempts;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", length = 50, nullable = false)
    private JobStatus finalStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "submit_failed_at")
    private OffsetDateTime submitFailedAt;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "heartbeat_at")
    private OffsetDateTime heartBeatAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "retry_count", columnDefinition = "TINYINT")
    private Byte retryCount;

}
