ALTER TABLE sync_attempt
    MODIFY COLUMN process_exit_code SMALLINT NULL
    COMMENT 'Exit code returned by operating system';

ALTER TABLE sync_attempt
    MODIFY COLUMN error_code VARCHAR(255)
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL
    COMMENT 'Application-defined error code';

ALTER TABLE sync_attempt
    MODIFY COLUMN error_message MEDIUMTEXT
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL
    COMMENT 'Detailed error message for debugging';