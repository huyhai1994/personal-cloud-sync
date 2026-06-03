package org.mini_lab.personal_cloud_sync.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mini_lab.personal_cloud_sync.enums.JobStatus;

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
    @Column(name = "final_status", length = 50)
    private JobStatus finalStatus;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;


    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "retry_count", columnDefinition = "TINYINT")
    private Byte retryCount;


}
