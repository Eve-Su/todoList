package com.zz.jobworker.infrastructure.persistence.repository;

import com.zz.platform.common.util.VectorUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeChunkWriteRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeChunkWriteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertChunk(Long documentId, String kbCode, int chunkNo, String chunkText, float[] embedding) {
        jdbcTemplate.update("""
                        INSERT INTO knowledge.knowledge_chunk
                        (document_id, kb_code, chunk_no, chunk_text, chunk_tokens, metadata_json, embedding)
                        VALUES (?, ?, ?, ?, ?, '{}'::jsonb, ?::public.vector)
                        """,
                documentId,
                kbCode,
                chunkNo,
                chunkText,
                estimateTokens(chunkText),
                VectorUtil.toPgVector(embedding));
    }

    public void deleteByDocumentId(Long documentId) {
        jdbcTemplate.update("DELETE FROM knowledge.knowledge_chunk WHERE document_id = ?", documentId);
    }

    private int estimateTokens(String text) {
        return text == null ? 0 : Math.max(1, text.length() / 2);
    }
}
