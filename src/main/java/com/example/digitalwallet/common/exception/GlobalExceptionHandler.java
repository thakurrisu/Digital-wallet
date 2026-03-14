package com.example.digitalwallet.common.exception;

import com.example.digitalwallet.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletException(WalletException ex){

        ErrorCode ec = ex.getErrorCode();
        return ResponseEntity.status(ec.getHttpStatus())
                                .body(ApiResponse.error(ex.getMessage()));

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        log.error("Unexpected error: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
