package com.zz.aiplatform.api.dto.knowledge;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResponse {

    private List<KnowledgeSearchItem> items;
}
