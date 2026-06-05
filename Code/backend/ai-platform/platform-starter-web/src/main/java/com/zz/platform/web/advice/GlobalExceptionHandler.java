package com.zz.platform.web.advice;

import com.zz.platform.common.exception.BaseBizException;
import com.zz.platform.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String PARAM_ERROR_CODE = "PARAM_ERROR";

    @ExceptionHandler(BaseBizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseBizException(BaseBizException ex, HttpServletRequest request) {
        log.warn("Business exception, uri={}, code={}, message={}", request.getRequestURI(), ex.getCode(), ex.getMessage());
        return ResponseEntity.ok(ApiResponse.fail(ex.getCode(), ex.getErrorMessage(), currentTraceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                   HttpServletRequest request) {
        String message = extractFieldErrorMessage(ex.getBindingResult().getFieldError());
        log.warn("Validation exception, uri={}, message={}", request.getRequestURI(), message);
        return ResponseEntity.badRequest().body(ApiResponse.fail(PARAM_ERROR_CODE, message, currentTraceId()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex, HttpServletRequest request) {
        String message = extractFieldErrorMessage(ex.getFieldError());
        log.warn("Bind exception, uri={}, message={}", request.getRequestURI(), message);
        return ResponseEntity.badRequest().body(ApiResponse.fail(PARAM_ERROR_CODE, message, currentTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception, uri={}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ApiResponse.DEFAULT_ERROR_CODE, ApiResponse.DEFAULT_ERROR_MESSAGE, currentTraceId()));
    }

    private String currentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    private String extractFieldErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return "请求参数校验失败";
        }
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
