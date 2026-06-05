package com.zz.platform.mq.consumer;

import com.zz.platform.common.response.code.CommonErrorCode;
import com.zz.platform.mq.message.BaseMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractMqConsumer {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TENANT_ID_KEY = "tenantId";
    private static final String USER_ID_KEY = "userId";

    protected void consume(BaseMqMessage message) {
        bindMdc(message);
        try {
            log.info("MQ consume start, taskId={}, taskType={}, businessId={}",
                    message.getTaskId(), message.getTaskType(), message.getBusinessId());
            doConsume(message);
            log.info("MQ consume success, taskId={}, taskType={}",
                    message.getTaskId(), message.getTaskType());
        } catch (Exception ex) {
            log.error("MQ consume failed, taskId={}, taskType={}",
                    message.getTaskId(), message.getTaskType(), ex);
            throw new IllegalStateException(defaultErrorCode() + ":" + CommonErrorCode.MQ_CONSUME_ERROR.getMessage(), ex);
        } finally {
            clearMdc();
        }
    }

    protected abstract void doConsume(BaseMqMessage message) throws Exception;

    protected String defaultErrorCode() {
        return CommonErrorCode.MQ_CONSUME_ERROR.getCode();
    }

    private void bindMdc(BaseMqMessage message) {
        putIfNotBlank(TRACE_ID_KEY, message.getTraceId());
        putIfNotBlank(TENANT_ID_KEY, message.getTenantId());
        putIfNotBlank(USER_ID_KEY, message.getUserId());
    }

    private void clearMdc() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(TENANT_ID_KEY);
        MDC.remove(USER_ID_KEY);
    }

    private void putIfNotBlank(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}
