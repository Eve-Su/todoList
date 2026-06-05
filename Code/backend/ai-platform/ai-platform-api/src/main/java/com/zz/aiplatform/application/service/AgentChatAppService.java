package com.zz.aiplatform.application.service;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;
import com.zz.aiplatform.runtime.runner.AgentRunner;
import org.springframework.stereotype.Service;

@Service
public class AgentChatAppService {

    private final AgentRunner agentRunner;

    public AgentChatAppService(AgentRunner agentRunner) {
        this.agentRunner = agentRunner;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        return agentRunner.run(request);
    }
}
