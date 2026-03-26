package com.example.digitalwallet.user.controller;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.user.dto.*;
import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request){
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login Successful",authResponse));
    }

    //logout
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(){
        userService.logout();
        return ResponseEntity.ok(ApiResponse.ok("Log out Successfull",null));
    }
    //get profile
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UUID userID){
        User user = userService.getUserById(userID);
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromUser(user)));
    }

    //updateprofile
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal UUID userID , @RequestBody UpdateRequest updateRequest){
        UserResponse userResponse = userService.updateProfile(userID,updateRequest);
        return ResponseEntity.ok(ApiResponse.ok(userResponse));
    }

    @PutMapping("/users/me/changePassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid ChangePasswordRequest request) {

        userService.updatePassword(userId, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Password changed successfully", null));
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UUID userId) {

        userService.delete(userId);
        return ResponseEntity.ok(
                ApiResponse.ok("Account deleted", null));
    }


}
