package com.zz.aiplatform.api.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchItem {

    private Long documentId;
    private Long chunkId;
    private Double score;
    private String content;
}
