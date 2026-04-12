package com.example.digitalwallet.common.exception;

import lombok.Getter;
import org.json.HTTP;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // User
    USER_NOT_FOUND("USR_001", "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USR_002", "User already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("USR_003", "Invalid credentials", HttpStatus.UNAUTHORIZED),

    // Wallet
    // Wallet errors
    WALLET_NOT_FOUND("WLT_001", "Wallet not found", HttpStatus.NOT_FOUND),
    WALLET_INACTIVE("WLT_002", "Wallet is not active", HttpStatus.FORBIDDEN),
    INSUFFICIENT_FUNDS("WLT_003", "Insufficient funds in wallet", HttpStatus.BAD_REQUEST),
    WALLET_ALREADY_EXISTS("WLT_004", "Wallet already exists for this user", HttpStatus.CONFLICT),


    // Transaction
    TRANSACTION_NOT_FOUND("TXN_001", "Transaction not found", HttpStatus.NOT_FOUND),
    DUPLICATE_TRANSACTION("TXN_002", "Duplicate transaction", HttpStatus.CONFLICT),
    INVALID_AMOUNT("TXN_003", "Amount must be greater than zero", HttpStatus.BAD_REQUEST),
    SELF_TRANSFER("TXN_004", "Cannot transfer to own wallet", HttpStatus.BAD_REQUEST),

    // Auth
    TOKEN_EXPIRED("AUTH_001", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_002", "Token is invalid", HttpStatus.UNAUTHORIZED),

    // Generic
    VALIDATION_FAILED("SYS_001", "Validation failed", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("SYS_002", "Internal error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    // Payment
    PAYMENT_FAILED("PAY_001", "Order Creation faile",HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("PAY_002", "Payment not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}