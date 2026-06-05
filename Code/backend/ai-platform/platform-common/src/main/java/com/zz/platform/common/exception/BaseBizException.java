package com.zz.platform.common.exception;

import java.io.Serial;

import lombok.Getter;

@Getter
public class BaseBizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String errorMessage;

    public BaseBizException(String code, String errorMessage) {
        super(errorMessage);
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public BaseBizException(String code, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
