package org.mini_lab.personal_cloud_sync.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "sync_config")
public class SyncConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "enabled", columnDefinition = "TINYINT(1)")
    private Boolean enabled;

    @OneToMany(mappedBy = "syncConfig", fetch = FetchType.LAZY)
    private List<SyncJob> syncJobs;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "next_scheduled_at")
    private OffsetDateTime nextScheduledAt;

    @Column(name = "source_path", length = 500)
    private String sourcePath;

    @Column(name = "target_path", length = 500)
    private String targetPath;

    @Column(name = "mount_path", length = 500)
    private String mountPath;

    @Column(name = "max_retry", columnDefinition = "TINYINT")
    private Byte maxRetry;

    @Column(name = "schedule_type")
    private ScheduleType scheduleType;

    @Column(name = "interval")
    private Short interval;

    @Column(name = "run_time")
    private LocalTime runTime;


}
