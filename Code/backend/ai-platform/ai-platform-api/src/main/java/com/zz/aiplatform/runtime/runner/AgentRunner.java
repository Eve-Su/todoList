package com.zz.aiplatform.runtime.runner;

import com.zz.aiplatform.api.dto.chat.AgentChatRequest;
import com.zz.aiplatform.api.dto.chat.AgentChatResponse;

public interface AgentRunner {

    AgentChatResponse run(AgentChatRequest request);
}
