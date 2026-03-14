package com.example.digitalwallet.common.exception;

import lombok.Getter;

@Getter
public class WalletException extends RuntimeException {

    private final ErrorCode errorCode;

    public WalletException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public WalletException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}