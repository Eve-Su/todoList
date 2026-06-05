package com.zz.platform.common.response.code;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {

    SUCCESS("0", "success"),
    PARAM_ERROR("COMMON_400", "请求参数错误"),
    UNAUTHORIZED("COMMON_401", "未授权访问"),
    FORBIDDEN("COMMON_403", "无权限访问"),
    NOT_FOUND("COMMON_404", "资源不存在"),
    SYSTEM_ERROR("COMMON_500", "系统异常"),
    MQ_SEND_ERROR("MQ_001", "MQ消息发送失败"),
    MQ_CONSUME_ERROR("MQ_002", "MQ消息消费失败");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
