package com.zz.platform.web.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TENANT_ID_KEY = "tenantId";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    public static final String USER_ID_KEY = "userId";
    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = firstNonBlank(request.getHeader(TRACE_ID_HEADER), generateTraceId());
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        String userId = request.getHeader(USER_ID_HEADER);

        MDC.put(TRACE_ID_KEY, traceId);
        putIfPresent(TENANT_ID_KEY, tenantId);
        putIfPresent(USER_ID_KEY, userId);

        response.setHeader(TRACE_ID_HEADER, traceId);
        if (tenantId != null && !tenantId.isBlank()) {
            response.setHeader(TENANT_ID_HEADER, tenantId);
        }
        if (userId != null && !userId.isBlank()) {
            response.setHeader(USER_ID_HEADER, userId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(TENANT_ID_KEY);
            MDC.remove(USER_ID_KEY);
        }
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
