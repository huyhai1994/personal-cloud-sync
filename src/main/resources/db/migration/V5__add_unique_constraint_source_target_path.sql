alter table sync_config
    modify column source_path varchar(255) not null;

alter table sync_config
    modify column target_path varchar(255) not null;

ALTER TABLE sync_config
    ADD CONSTRAINT uk_sync_config_source_target_path UNIQUE (source_path, target_path);