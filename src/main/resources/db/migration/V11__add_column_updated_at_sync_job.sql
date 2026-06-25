alter table sync_job
    add column submitted_at TIMESTAMP;
alter table sync_job
    add column submit_failed_at TIMESTAMP;
alter table sync_job
    add column heartbeat_at timestamp;
alter table sync_job
    add column updated_at TIMESTAMP;

