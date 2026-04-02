package com.example.digitalwallet.user.service;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.common.config.JwtAuthFilter;
import com.example.digitalwallet.common.config.JwtService;
import com.example.digitalwallet.common.exception.ErrorCode;
import com.example.digitalwallet.common.exception.WalletException;
import com.example.digitalwallet.user.dto.*;
import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.model.UserStatus;
import com.example.digitalwallet.user.repo.UserRepository;
import com.example.digitalwallet.wallet.service.WalletService;
import jdk.jshell.spi.ExecutionControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    UserRepository userRepository;

    JwtService jsw;

    PasswordEncoder pswdEncode;

    WalletService walletService;

    public UserServiceImpl(UserRepository userRepository, JwtService jsw, PasswordEncoder pswdEncode , WalletService walletService) {
        this.userRepository = userRepository;
        this.jsw = jsw;
        this.pswdEncode = pswdEncode;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering user : " + request.getName());
        String email = request.getEmail();
        if(userRepository.existsByEmail(email))
            throw new WalletException(ErrorCode.USER_ALREADY_EXISTS,"Email already registered for email " + request.getEmail());

            User user = User.builder().userName(request.getName())
                            .email(email)
                             .password(pswdEncode.encode(request.getPassword().toLowerCase().trim()))
                                .build();
            User savedUser = userRepository.save(user);
            walletService.createWallet(savedUser);
            return UserResponse.fromUser(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailAndIsDeletedFalse(
                        request.getEmail().toLowerCase().trim())
                .orElseThrow(()->new WalletException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new WalletException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!pswdEncode.matches(request.getPassword(),
                user.getPassword())) {
            throw new WalletException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jsw.generateToken(
                user.getId(), user.getEmail());
        log.info("" + user.getId());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserResponse.fromUser(user))
                .build();

    }

    //what if update ke samay duplicate email de dia
    @Override
    public UserResponse updateProfile(UUID id, UpdateRequest request) {
        User user = findActiveUserById(id);

        if(StringUtils.hasText(request.getEmail())) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail))
                    throw new WalletException(ErrorCode.USER_ALREADY_EXISTS);
            }
            user.setEmail(newEmail);
        }
        if(StringUtils.hasText(request.getName()))
            user.setUserName(request.getName().trim());

        return UserResponse.fromUser(userRepository.save(user));

    }

    @Override
    public UserResponse updatePassword(UUID id, ChangePasswordRequest request) {
        User user = findActiveUserById(id);
        if(!pswdEncode.matches(request.getCurrentPassword(),user.getPassword())){
            throw new WalletException(ErrorCode.INVALID_CREDENTIALS);
        }
        if(pswdEncode.matches(request.getNewPassword(), user.getPassword()))
            throw new WalletException(ErrorCode.VALIDATION_FAILED,"Current Password and New Password must not be same");

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new WalletException(ErrorCode.VALIDATION_FAILED,
                    "Passwords do not match");
        }

        user.setPassword(pswdEncode.encode(request.getCurrentPassword()));
        User updatedUser = userRepository.save(user);
        return UserResponse.fromUser(updatedUser);
    }

    @Override
    public ApiResponse<Void> logout() {
        return null;
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(()->new WalletException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public ApiResponse<Void> delete(UUID id) {
        User user = findActiveUserById(id);

        userRepository.softDeleteUser(
                user.getId(),
                UserStatus.DELETED,
                LocalDateTime.now());
        return null;
    }


    private User findActiveUserById(UUID userId) {
        return userRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() ->
                        new WalletException(ErrorCode.USER_NOT_FOUND));
    }
}
