package com.zz.aiplatform.infrastructure.client;

import java.util.Collections;

import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchRequest;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class KnowledgeHttpClient implements KnowledgeClient {

    private final RestTemplate restTemplate;

    public KnowledgeHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public KnowledgeSearchResponse search(KnowledgeSearchRequest request) {
        log.info("Knowledge search placeholder invoked, kbCode={}, topK={}", request.getKbCode(), request.getTopK());
        return KnowledgeSearchResponse.builder()
                .items(Collections.emptyList())
                .build();
    }
}
