package com.zz.knowledge.infrastructure.persistence.repository;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zz.platform.common.util.VectorUtil;
import com.zz.knowledge.infrastructure.persistence.entity.KnowledgeChunkEntity;
import com.zz.knowledge.infrastructure.persistence.mapper.KnowledgeChunkMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeChunkRepository {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeChunkRepository(KnowledgeChunkMapper knowledgeChunkMapper, JdbcTemplate jdbcTemplate) {
        this.knowledgeChunkMapper = knowledgeChunkMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<KnowledgeChunkEntity> searchText(String kbCode, String query, int topK) {
        return knowledgeChunkMapper.selectList(new LambdaQueryWrapper<KnowledgeChunkEntity>()
                .eq(KnowledgeChunkEntity::getKbCode, kbCode)
                .like(KnowledgeChunkEntity::getChunkText, query)
                .orderByAsc(KnowledgeChunkEntity::getChunkNo)
                .last("limit " + topK));
    }

    public List<KnowledgeChunkEntity> searchByEmbedding(String kbCode, float[] queryEmbedding, int topK) {
        String vector = VectorUtil.toPgVector(queryEmbedding);
        return jdbcTemplate.query("""
                        SELECT id, document_id, kb_code, chunk_no, chunk_text, chunk_tokens, metadata_json, created_at
                        FROM knowledge.knowledge_chunk
                        WHERE kb_code = ?
                          AND embedding IS NOT NULL
                        ORDER BY embedding OPERATOR(public.<=>) ?::public.vector
                        LIMIT ?
                        """,
                (rs, rowNum) -> {
                    KnowledgeChunkEntity entity = new KnowledgeChunkEntity();
                    entity.setId(rs.getLong("id"));
                    entity.setDocumentId(rs.getLong("document_id"));
                    entity.setKbCode(rs.getString("kb_code"));
                    entity.setChunkNo(rs.getInt("chunk_no"));
                    entity.setChunkText(rs.getString("chunk_text"));
                    entity.setChunkTokens((Integer) rs.getObject("chunk_tokens"));
                    entity.setMetadataJson(rs.getString("metadata_json"));
                    entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return entity;
                },
                kbCode, vector, topK);
    }
}
