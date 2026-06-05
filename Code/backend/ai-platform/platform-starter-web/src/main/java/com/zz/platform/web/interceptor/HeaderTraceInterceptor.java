package com.zz.platform.web.interceptor;

import java.io.IOException;

import com.zz.platform.common.constant.TraceConstant;
import com.zz.platform.common.util.TraceUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HeaderTraceInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        putHeaderIfPresent(request, TraceConstant.TRACE_ID_HEADER, TraceUtil.currentTraceId());
        putHeaderIfPresent(request, TraceConstant.TENANT_ID_HEADER, TraceUtil.currentTenantId());
        putHeaderIfPresent(request, TraceConstant.USER_ID_HEADER, TraceUtil.currentUserId());
        return execution.execute(request, body);
    }

    private void putHeaderIfPresent(HttpRequest request, String headerName, String value) {
        if (value != null && !value.isBlank()) {
            request.getHeaders().set(headerName, value);
        }
    }
}
