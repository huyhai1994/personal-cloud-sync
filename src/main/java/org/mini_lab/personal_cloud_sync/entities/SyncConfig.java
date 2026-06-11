package org.mini_lab.personal_cloud_sync.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "sync_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sync_config_source_target_path",
                        columnNames = {"source_path", "target_path"}
                )
        })
public class SyncConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "enabled", columnDefinition = "TINYINT(1)")
    private Boolean enabled = Boolean.TRUE;

    @OneToMany(mappedBy = "syncConfig", fetch = FetchType.LAZY)
    private List<SyncJob> syncJobs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "next_scheduled_at")
    private OffsetDateTime nextScheduledAt;

    @Column(name = "source_path", length = 500, nullable = false)
    private String sourcePath;

    @Column(name = "target_path", length = 500, nullable = false)
    private String targetPath;

    @Column(name = "max_retry", columnDefinition = "TINYINT", length = 10)
    private Byte maxRetry = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type")
    private ScheduleType scheduleType = ScheduleType.MANUAL;

    @Column(name = "sync_interval")
    private Short scheduleInterval;

    @Column(name = "run_time")
    private LocalTime runTime;

}
