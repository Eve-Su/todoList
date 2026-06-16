package com.zz.aiplatform.infrastructure.client;

import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchRequest;
import com.zz.aiplatform.api.dto.knowledge.KnowledgeSearchResponse;
import com.zz.platform.common.exception.BaseBizException;
import com.zz.platform.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@ConfigurationProperties(prefix = "platform.clients.knowledge-service")
public class KnowledgeHttpClient implements KnowledgeClient {

    private final RestTemplate restTemplate;
    private String baseUrl;

    public KnowledgeHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public KnowledgeSearchResponse search(KnowledgeSearchRequest request) {
        String url = trimTrailingSlash(baseUrl) + "/knowledge/search";
        log.info("Knowledge search request, url={}, kbCode={}, topK={}", url, request.getKbCode(), request.getTopK());
        try {
            ResponseEntity<ApiResponse<KnowledgeSearchResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<>() {
                    });
            ApiResponse<KnowledgeSearchResponse> body = response.getBody();
            if (body == null || !ApiResponse.SUCCESS_CODE.equals(body.getCode())) {
                throw new BaseBizException("KNOWLEDGE_SEARCH_ERROR", "知识检索服务返回异常");
            }
            return body.getData();
        } catch (BaseBizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Knowledge search failed, url={}", url, ex);
            throw new BaseBizException("KNOWLEDGE_SEARCH_ERROR", "知识检索服务调用失败", ex);
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new BaseBizException("KNOWLEDGE_CLIENT_NOT_CONFIGURED", "knowledge-service地址未配置");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
