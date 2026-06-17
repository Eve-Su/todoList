-- job-worker owns schema job_work.
-- Tables in this migration are used to record async MQ task execution and retry state.

CREATE SCHEMA IF NOT EXISTS job_work;

CREATE TABLE IF NOT EXISTS job_work.job_task_log (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    topic_name VARCHAR(128),
    tag_name VARCHAR(128),
    business_id VARCHAR(64),
    trace_id VARCHAR(64),
    tenant_id VARCHAR(64),
    source_service VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    result_json JSONB,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_task_log_task_id ON job_work.job_task_log(task_id);
CREATE INDEX IF NOT EXISTS idx_job_task_log_task_type ON job_work.job_task_log(task_type);
CREATE INDEX IF NOT EXISTS idx_job_task_log_status ON job_work.job_task_log(status);
CREATE INDEX IF NOT EXISTS idx_job_task_log_trace_id ON job_work.job_task_log(trace_id);
CREATE INDEX IF NOT EXISTS idx_job_task_log_created_at ON job_work.job_task_log(created_at DESC);

CREATE TABLE IF NOT EXISTS job_work.job_retry_log (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    task_type VARCHAR(64) NOT NULL,
    retry_no INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_message TEXT,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_retry_log_task_id ON job_work.job_retry_log(task_id);
CREATE INDEX IF NOT EXISTS idx_job_retry_log_status ON job_work.job_retry_log(status);
CREATE INDEX IF NOT EXISTS idx_job_retry_log_next_retry_at ON job_work.job_retry_log(next_retry_at);
