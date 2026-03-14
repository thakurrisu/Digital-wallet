package com.example.digitalwallet.user.controller;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.user.dto.AuthResponse;
import com.example.digitalwallet.user.dto.RegisterRequest;
import com.example.digitalwallet.user.dto.UserResponse;
import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //register
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid  RegisterRequest request){
        UserResponse regiterReponse = userService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("User Created",regiterReponse));

    }
    //login
    //logout
    //updateprofile
    //update password
}
