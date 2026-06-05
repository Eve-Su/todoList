package com.zz.platform.common.util;

import java.util.UUID;

import com.zz.platform.common.constant.TraceConstant;
import org.slf4j.MDC;

public final class TraceUtil {

    private TraceUtil() {
    }

    public static String currentTraceId() {
        return MDC.get(TraceConstant.TRACE_ID_KEY);
    }

    public static String currentTenantId() {
        return MDC.get(TraceConstant.TENANT_ID_KEY);
    }

    public static String currentUserId() {
        return MDC.get(TraceConstant.USER_ID_KEY);
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
