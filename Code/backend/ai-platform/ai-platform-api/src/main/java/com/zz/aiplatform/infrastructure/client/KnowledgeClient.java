package com.zz.aiplatform.infrastructure.client;

import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchRequest;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchResponse;

public interface KnowledgeClient {

    KnowledgeSearchResponse search(KnowledgeSearchRequest request);
}
