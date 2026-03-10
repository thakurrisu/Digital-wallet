package com.example.digitalwallet.user.model;

public enum UserStatus {

    ACTIVE,
    //User has been blocked by admin
    INACTIVE,
    //User deleted the account
    //Useful for soft delete
    DELETED
}
