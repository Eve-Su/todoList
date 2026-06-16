package com.zz.aiplatform.runtime.planner;

import com.zz.aiplatform.domain.agent.model.AgentAction;
import com.zz.aiplatform.runtime.context.PromptContext;

public interface Planner {

    AgentAction decide(PromptContext context);
}
