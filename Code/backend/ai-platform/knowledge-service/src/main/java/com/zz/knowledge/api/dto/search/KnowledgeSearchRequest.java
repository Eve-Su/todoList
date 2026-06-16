package com.zz.knowledge.api.dto.search;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeSearchRequest {

    @NotBlank(message = "kbCode不能为空")
    private String kbCode;

    @NotBlank(message = "query不能为空")
    private String query;

    private Integer topK = 5;
}
