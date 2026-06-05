package com.zz.platform.common.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_CODE = "0";
    public static final String DEFAULT_SUCCESS_MESSAGE = "success";
    public static final String DEFAULT_ERROR_CODE = "SYSTEM_ERROR";
    public static final String DEFAULT_ERROR_MESSAGE = "系统异常";

    private String code;
    private String message;
    private T data;
    private String traceId;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(DEFAULT_SUCCESS_MESSAGE)
                .data(data)
                .traceId(traceId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> success(String traceId) {
        return success(null, traceId);
    }

    public static <T> ApiResponse<T> fail(String code, String message, String traceId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .traceId(traceId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ApiResponse<T> fail(String message, String traceId) {
        return fail(DEFAULT_ERROR_CODE, message, traceId);
    }
}
