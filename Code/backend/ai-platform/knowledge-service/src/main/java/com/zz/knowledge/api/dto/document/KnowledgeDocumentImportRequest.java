package com.zz.knowledge.api.dto.document;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeDocumentImportRequest {

    @NotBlank(message = "kbCode不能为空")
    private String kbCode;

    @NotBlank(message = "docName不能为空")
    private String docName;

    private String sourceType = "TEXT";
    private String sourceUri;

    @NotBlank(message = "contentText不能为空")
    private String contentText;
}
