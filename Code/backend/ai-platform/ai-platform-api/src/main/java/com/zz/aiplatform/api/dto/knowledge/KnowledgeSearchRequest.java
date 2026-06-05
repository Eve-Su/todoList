package com.zz.aiplatform.api.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchRequest {

    private String kbCode;
    private String query;
    private Integer topK;
}
