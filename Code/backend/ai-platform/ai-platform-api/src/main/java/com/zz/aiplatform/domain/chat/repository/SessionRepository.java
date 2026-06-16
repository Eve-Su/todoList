package com.zz.aiplatform.domain.chat.repository;

import com.zz.aiplatform.domain.chat.model.AgentSession;

public interface SessionRepository {

    AgentSession findBySessionId(String sessionId);

    void save(AgentSession session);
}
