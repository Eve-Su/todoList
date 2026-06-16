package com.zz.knowledge.api.controller;

import com.zz.knowledge.api.dto.search.KnowledgeSearchRequest;
import com.zz.knowledge.api.dto.search.KnowledgeSearchResponse;
import com.zz.knowledge.application.service.KnowledgeSearchAppService;
import com.zz.platform.common.response.ApiResponse;
import com.zz.platform.common.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "KnowledgeSearchController", description = "知识检索接口")
@RestController
@RequestMapping("/knowledge")
public class KnowledgeSearchController {

    private final KnowledgeSearchAppService knowledgeSearchAppService;

    public KnowledgeSearchController(KnowledgeSearchAppService knowledgeSearchAppService) {
        this.knowledgeSearchAppService = knowledgeSearchAppService;
    }

    @Operation(summary = "知识检索")
    @PostMapping("/search")
    public ApiResponse<KnowledgeSearchResponse> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return ApiResponse.success(knowledgeSearchAppService.search(request), TraceUtil.currentTraceId());
    }
}
