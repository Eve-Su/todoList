package com.zz.aiplatform.infrastructure.persistence.repository;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zz.aiplatform.domain.chat.model.AgentSession;
import com.zz.aiplatform.domain.chat.repository.SessionRepository;
import com.zz.aiplatform.infrastructure.persistence.entity.AgentSessionEntity;
import com.zz.aiplatform.infrastructure.persistence.mapper.AgentSessionMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DbSessionRepository implements SessionRepository {

    private final AgentSessionMapper agentSessionMapper;

    public DbSessionRepository(AgentSessionMapper agentSessionMapper) {
        this.agentSessionMapper = agentSessionMapper;
    }

    @Override
    public AgentSession findBySessionId(String sessionId) {
        AgentSessionEntity entity = agentSessionMapper.selectOne(new LambdaQueryWrapper<AgentSessionEntity>()
                .eq(AgentSessionEntity::getSessionId, sessionId)
                .last("limit 1"));
        return toDomain(entity);
    }

    @Override
    public void save(AgentSession session) {
        AgentSessionEntity existing = agentSessionMapper.selectOne(new LambdaQueryWrapper<AgentSessionEntity>()
                .eq(AgentSessionEntity::getSessionId, session.getSessionId())
                .last("limit 1"));
        if (existing == null) {
            AgentSessionEntity entity = toEntity(session);
            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            agentSessionMapper.insert(entity);
            return;
        }

        agentSessionMapper.update(null, new LambdaUpdateWrapper<AgentSessionEntity>()
                .eq(AgentSessionEntity::getSessionId, session.getSessionId())
                .set(AgentSessionEntity::getAgentCode, session.getAgentCode())
                .set(AgentSessionEntity::getUserId, session.getUserId())
                .set(AgentSessionEntity::getSummary, session.getSummary())
                .set(AgentSessionEntity::getLastMessageAt, session.getLastMessageAt())
                .set(AgentSessionEntity::getUpdatedAt, LocalDateTime.now()));
    }

    private AgentSession toDomain(AgentSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        return AgentSession.builder()
                .sessionId(entity.getSessionId())
                .agentCode(entity.getAgentCode())
                .userId(entity.getUserId())
                .summary(entity.getSummary())
                .lastMessageAt(entity.getLastMessageAt())
                .build();
    }

    private AgentSessionEntity toEntity(AgentSession session) {
        AgentSessionEntity entity = new AgentSessionEntity();
        entity.setSessionId(session.getSessionId());
        entity.setAgentCode(session.getAgentCode());
        entity.setUserId(session.getUserId());
        entity.setSummary(session.getSummary());
        entity.setLastMessageAt(session.getLastMessageAt());
        return entity;
    }
}
