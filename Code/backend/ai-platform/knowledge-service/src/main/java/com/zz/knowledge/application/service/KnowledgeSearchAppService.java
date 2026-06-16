package com.zz.knowledge.application.service;

import java.util.List;

import com.zz.knowledge.api.dto.search.KnowledgeSearchItem;
import com.zz.knowledge.api.dto.search.KnowledgeSearchRequest;
import com.zz.knowledge.api.dto.search.KnowledgeSearchResponse;
import com.zz.knowledge.infrastructure.llm.EmbeddingFacade;
import com.zz.knowledge.infrastructure.persistence.repository.KnowledgeChunkRepository;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeSearchAppService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EmbeddingFacade embeddingFacade;

    public KnowledgeSearchAppService(KnowledgeChunkRepository knowledgeChunkRepository, EmbeddingFacade embeddingFacade) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.embeddingFacade = embeddingFacade;
    }

    public KnowledgeSearchResponse search(KnowledgeSearchRequest request) {
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? 5 : request.getTopK();
        List<KnowledgeSearchItem> items = knowledgeChunkRepository.searchByEmbedding(
                        request.getKbCode(), embeddingFacade.embed(request.getQuery()), topK)
                .stream()
                .map(chunk -> KnowledgeSearchItem.builder()
                        .documentId(chunk.getDocumentId())
                        .chunkId(chunk.getId())
                        .score(1.0D)
                        .content(chunk.getChunkText())
                        .build())
                .toList();
        if (items.isEmpty()) {
            items = knowledgeChunkRepository.searchText(request.getKbCode(), request.getQuery(), topK)
                    .stream()
                    .map(chunk -> KnowledgeSearchItem.builder()
                            .documentId(chunk.getDocumentId())
                            .chunkId(chunk.getId())
                            .score(1.0D)
                            .content(chunk.getChunkText())
                            .build())
                    .toList();
        }
        return KnowledgeSearchResponse.builder()
                .items(items)
                .build();
    }
}
