package com.example.digitalwallet.common;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

    private final String message;
    private final LocalDateTime timestamp;
    private final boolean success;
    private final T data;

    //200 - with success and data
    public static<T> ApiResponse<T> ok(String message ,T data){
       return  ApiResponse.<T>builder()
               .message(message)
               .data(data)
               .success(true)
               .timestamp(LocalDateTime.now()).build();
    }

    public static<T> ApiResponse<T> ok(String message){
        return  ApiResponse.<T>builder()
                .message(message)
                .data(null)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static<T> ApiResponse<Void> error(String message){
        return  ApiResponse.<Void>builder()
                .message(message)
                .data(null)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
