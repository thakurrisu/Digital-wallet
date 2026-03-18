package com.example.digitalwallet.wallet.service;

import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.wallet.dto.WalletResponse;
import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.repo.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService{

    @Autowired
    WalletRepository walletRepository;

    @Override
    public WalletResponse createWallet(User user) {
        return null;
    }

    @Override
    public WalletResponse getWalletByUserId(UUID userId) {
        return null;
    }

    @Override
    public Wallet getActiveWalletByUserId(UUID userId) {
        return null;
    }

    @Override
    public void credit(UUID wallteId, BigDecimal amount) {

    }

    @Override
    public void debit(UUID wallteId, BigDecimal amount) {

    }

    @Override
    public void freezeWallet(UUID walletId) {

    }
}
