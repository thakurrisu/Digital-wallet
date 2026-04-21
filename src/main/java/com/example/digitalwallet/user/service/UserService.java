package com.example.digitalwallet.user.service;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.user.dto.*;
import com.example.digitalwallet.user.model.User;

import java.util.UUID;

public interface UserService {

    //Register User
     UserResponse register(RegisterRequest request);

     //Login User
     AuthResponse login(LoginRequest request);

    //Update User
    UserResponse updateProfile(UUID id,UpdateRequest request);

    //Update Password
    UserResponse updatePassword(UUID id,ChangePasswordRequest request, String token);

    //Logout
    void logout(String token);

    //get user by id
    User getUserById(UUID id);

    //delete
    void delete(UUID id);
}
