CREATE TABLE IF NOT EXISTS `sync_config`
(
    `id`
                        SMALLINT
                                     NOT
                                         NULL
        AUTO_INCREMENT
        UNIQUE,
    `enabled`
                        TINYINT
                                     NOT
                                         NULL,
    `created_at`
                        TIMESTAMP
                                     NOT
                                         NULL,
    `source_path`
                        VARCHAR(500) NOT NULL,
    `mount_path`        VARCHAR(500) NOT NULL,
    `target_path`       VARCHAR(500) NOT NULL,
    `updated_at`        TIMESTAMP,
    `next_scheduled_at` TIMESTAMP,
    `max_retry`         TINYINT      NOT NULL,
    `schedule_type`     TINYINT      NOT NULL,
    `interval`          SMALLINT,
    `run_time`          TIME,
    PRIMARY KEY
        (
         `id`
            )
);


CREATE TABLE IF NOT EXISTS `sync_job`
(
    `id`
        INTEGER
        NOT
            NULL
        AUTO_INCREMENT
        UNIQUE,
    `sync_config_id`
        SMALLINT,
    `final_status`
        TINYINT
        NOT
            NULL,
    `created_at`
        TIMESTAMP,
    `start_at`
        TIMESTAMP,
    `finished_at`
        TIMESTAMP,
    `retry_count`
        TINYINT,
    PRIMARY
        KEY
        (
         `id`
            )
);


CREATE TABLE IF NOT EXISTS `sync_attempt`
(
    `id`
                        INTEGER
        NOT
            NULL
        AUTO_INCREMENT
        UNIQUE,
    `sync_job_id`
                        INTEGER,
    `attempt_status`
                        TINYINT
        NOT
            NULL,
    `start_at`
                        TIMESTAMP
        NOT
            NULL,
    `finished_at`
                        TIMESTAMP
        NOT
            NULL,
    `error_code`
                        VARCHAR(255),
    `error_message`     TEXT(65535),
    `attempt_no`        TINYINT,
    `process_exit_code` SMALLINT,
    PRIMARY KEY
        (
         `id`
            )
);


ALTER TABLE `sync_job`
    ADD FOREIGN KEY (`sync_config_id`) REFERENCES `sync_config` (`id`)
        ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE `sync_attempt`
    ADD FOREIGN KEY (`sync_job_id`) REFERENCES `sync_job` (`id`)
        ON UPDATE NO ACTION ON DELETE NO ACTION;