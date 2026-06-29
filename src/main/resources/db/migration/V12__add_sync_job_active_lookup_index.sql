CREATE INDEX idx_sync_config_status
    ON sync_job (sync_config_id, final_status);