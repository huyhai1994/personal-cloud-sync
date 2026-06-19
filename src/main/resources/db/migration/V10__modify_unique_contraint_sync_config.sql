alter table sync_config
    drop index uk_sync_config_source_target_path;

ALTER TABLE sync_config
    ADD CONSTRAINT uk_sync_config_source_target_path_scheduletype UNIQUE (source_path, target_path, schedule_type);
