package com.zz.knowledge.api.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocumentImportResponse {

    private String taskId;
    private String docCode;
    private String status;
}
