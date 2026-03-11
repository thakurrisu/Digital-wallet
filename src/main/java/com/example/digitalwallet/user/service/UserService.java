package com.example.digitalwallet.user.service;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.user.dto.*;

import java.util.UUID;

public interface UserService {

    //Register User
     UserResponse register(RegisterRequest request);

     //Login User
     AuthResponse login(LoginRequest request);

    //Update User
    UserResponse updateProfile(UUID id,UpdateRequest request);

    //Update Password
    UserResponse updatePassword(UUID id,ChangePasswordRequest request);

    //Logout
    ApiResponse<Void> logout();

    //get user by id
    UserResponse getUserById(UUID id);

    //delete
    ApiResponse<Void> delete(UUID id);
}
