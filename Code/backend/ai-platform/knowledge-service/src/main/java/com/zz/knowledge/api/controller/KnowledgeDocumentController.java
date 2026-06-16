package com.zz.knowledge.api.controller;

import com.zz.knowledge.api.dto.document.KnowledgeDocumentImportRequest;
import com.zz.knowledge.api.dto.document.KnowledgeDocumentImportResponse;
import com.zz.knowledge.application.service.KnowledgeDocumentImportAppService;
import com.zz.platform.common.response.ApiResponse;
import com.zz.platform.common.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "KnowledgeDocumentController", description = "知识文档接口")
@RestController
@RequestMapping("/knowledge/document")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentImportAppService knowledgeDocumentImportAppService;

    public KnowledgeDocumentController(KnowledgeDocumentImportAppService knowledgeDocumentImportAppService) {
        this.knowledgeDocumentImportAppService = knowledgeDocumentImportAppService;
    }

    @Operation(summary = "导入知识文档")
    @PostMapping("/import")
    public ApiResponse<KnowledgeDocumentImportResponse> importDocument(
            @Valid @RequestBody KnowledgeDocumentImportRequest request) {
        return ApiResponse.success(knowledgeDocumentImportAppService.importDocument(request), TraceUtil.currentTraceId());
    }
}
