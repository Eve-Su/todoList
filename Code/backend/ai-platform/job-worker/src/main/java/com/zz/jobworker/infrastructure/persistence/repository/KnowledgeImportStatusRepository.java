package com.zz.jobworker.infrastructure.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeImportStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeImportStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void markImporting(String taskId, String docCode) {
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_import_task
                        SET status = 'IMPORTING', updated_at = CURRENT_TIMESTAMP
                        WHERE task_id = ?
                        """,
                taskId);
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_document
                        SET status = 'IMPORTING', updated_at = CURRENT_TIMESTAMP
                        WHERE doc_code = ?
                        """,
                docCode);
    }

    public void markReady(String taskId, String docCode) {
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_import_task
                        SET status = 'READY', error_message = NULL, updated_at = CURRENT_TIMESTAMP
                        WHERE task_id = ?
                        """,
                taskId);
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_document
                        SET status = 'READY', updated_at = CURRENT_TIMESTAMP
                        WHERE doc_code = ?
                        """,
                docCode);
    }

    public void markFailed(String taskId, String docCode, String errorMessage) {
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_import_task
                        SET status = 'FAILED', error_message = ?, retry_count = retry_count + 1, updated_at = CURRENT_TIMESTAMP
                        WHERE task_id = ?
                        """,
                errorMessage, taskId);
        jdbcTemplate.update("""
                        UPDATE knowledge.knowledge_document
                        SET status = 'FAILED', updated_at = CURRENT_TIMESTAMP
                        WHERE doc_code = ?
                        """,
                docCode);
    }
}
