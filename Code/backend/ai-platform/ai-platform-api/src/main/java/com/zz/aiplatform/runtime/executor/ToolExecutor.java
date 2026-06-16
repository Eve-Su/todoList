package com.zz.aiplatform.runtime.executor;

import com.zz.aiplatform.domain.agent.model.AgentAction;
import com.zz.aiplatform.runtime.context.PromptContext;

public interface ToolExecutor {

    String execute(AgentAction action, PromptContext context);
}
