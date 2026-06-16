package com.zz.jobworker.infrastructure.persistence.repository;

import com.zz.platform.mq.message.BaseMqMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JobTaskLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobTaskLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void start(BaseMqMessage message, String topicName) {
        jdbcTemplate.update("""
                        INSERT INTO job_work.job_task_log
                        (task_id, task_type, topic_name, business_id, trace_id, tenant_id, source_service, status, started_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'RUNNING', CURRENT_TIMESTAMP)
                        """,
                message.getTaskId(),
                message.getTaskType(),
                topicName,
                message.getBusinessId(),
                message.getTraceId(),
                message.getTenantId(),
                message.getSourceService());
    }

    public void success(String taskId, String resultJson) {
        jdbcTemplate.update("""
                        UPDATE job_work.job_task_log
                        SET status = 'SUCCESS', result_json = ?::jsonb, finished_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                        WHERE id = (
                            SELECT id FROM job_work.job_task_log
                            WHERE task_id = ?
                            ORDER BY created_at DESC
                            LIMIT 1
                        )
                        """,
                resultJson, taskId);
    }

    public void failed(String taskId, String errorMessage) {
        jdbcTemplate.update("""
                        UPDATE job_work.job_task_log
                        SET status = 'FAILED', error_message = ?, finished_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                        WHERE id = (
                            SELECT id FROM job_work.job_task_log
                            WHERE task_id = ?
                            ORDER BY created_at DESC
                            LIMIT 1
                        )
                        """,
                errorMessage, taskId);
    }
}
