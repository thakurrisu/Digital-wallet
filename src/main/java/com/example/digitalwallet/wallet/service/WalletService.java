package com.example.digitalwallet.wallet.service;

import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.wallet.dto.WalletResponse;
import com.example.digitalwallet.wallet.model.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    //createWallet
    WalletResponse createWallet(User user);

    //getWallet
    WalletResponse getWalletByUserId(UUID userId);

    //For Transaction
    Wallet getActiveWalletByUserId(UUID userId);

    //credit
    void credit(UUID wallteId,BigDecimal amount);

    //debit
    void debit(UUID wallteId,BigDecimal amount);

    //freeze
    WalletResponse freezeWallet(UUID userId);

    //get wallet by ID
    Wallet getActiveWalletById(UUID walletId);
}
