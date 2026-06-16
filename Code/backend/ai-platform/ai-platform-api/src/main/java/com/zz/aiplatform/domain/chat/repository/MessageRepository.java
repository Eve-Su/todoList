package com.zz.aiplatform.domain.chat.repository;

import java.util.List;

import com.zz.aiplatform.domain.chat.model.AgentMessage;

public interface MessageRepository {

    void save(AgentMessage message);

    List<AgentMessage> findRecentBySessionId(String sessionId, int limit);
}
