package com.zz.aiplatform.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zz.aiplatform.domain.chat.model.AgentMessage;
import com.zz.aiplatform.domain.chat.model.AgentMessageRole;
import com.zz.aiplatform.domain.chat.repository.MessageRepository;
import com.zz.aiplatform.infrastructure.persistence.entity.AgentMessageEntity;
import com.zz.aiplatform.infrastructure.persistence.mapper.AgentMessageMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DbMessageRepository implements MessageRepository {

    private final AgentMessageMapper agentMessageMapper;

    public DbMessageRepository(AgentMessageMapper agentMessageMapper) {
        this.agentMessageMapper = agentMessageMapper;
    }

    @Override
    public void save(AgentMessage message) {
        AgentMessageEntity entity = toEntity(message);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        agentMessageMapper.insert(entity);
    }

    @Override
    public List<AgentMessage> findRecentBySessionId(String sessionId, int limit) {
        return agentMessageMapper.selectList(new LambdaQueryWrapper<AgentMessageEntity>()
                        .eq(AgentMessageEntity::getSessionId, sessionId)
                        .orderByDesc(AgentMessageEntity::getCreatedAt)
                        .last("limit " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AgentMessage toDomain(AgentMessageEntity entity) {
        return AgentMessage.builder()
                .sessionId(entity.getSessionId())
                .role(AgentMessageRole.valueOf(entity.getMessageRole()))
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AgentMessageEntity toEntity(AgentMessage message) {
        AgentMessageEntity entity = new AgentMessageEntity();
        entity.setSessionId(message.getSessionId());
        entity.setMessageRole(message.getRole().name());
        entity.setContent(message.getContent());
        entity.setCreatedAt(message.getCreatedAt());
        return entity;
    }
}
