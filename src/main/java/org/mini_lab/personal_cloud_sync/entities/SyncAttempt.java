package org.mini_lab.personal_cloud_sync.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mini_lab.personal_cloud_sync.enums.SyncErrorCode;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_attempt")
@Getter
@Setter
public class SyncAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_job_id", referencedColumnName = "id")
    private SyncJob syncJob;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_code")
    private SyncErrorCode errorCode;

    @Column(name = "error_message", columnDefinition = "MEDIUMTEXT")
    private String errorMessage;

    @Column(name = "attempt_no", columnDefinition = "TINYINT")
    private Byte attemptNumbers;

    @Column(name = "process_exit_code", columnDefinition = "SMALLINT")
    private Short processExitCode;
}
