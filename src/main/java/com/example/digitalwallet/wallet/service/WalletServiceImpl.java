package com.example.digitalwallet.wallet.service;

import com.example.digitalwallet.common.exception.ErrorCode;
import com.example.digitalwallet.common.exception.WalletException;
import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.wallet.dto.WalletResponse;
import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.model.WalletStatus;
import com.example.digitalwallet.wallet.repo.WalletRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService{


    WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public WalletResponse createWallet(User user) {
        if(walletRepository.findByUserId(user.getId()).isPresent()){
            log.info("Wallet exists for userId " +  user.getId());
                throw new WalletException(ErrorCode.WALLET_ALREADY_EXISTS, "Wallet exists for the user");
        }
        Wallet wallet = Wallet.builder().user(user).build();
        Wallet saved = walletRepository.save(wallet);
        return WalletResponse.fromWallet(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(UUID userId) {
        if(!walletRepository.findByUserId(userId).isPresent()){
            log.info("Wallet does not exists for userId " +  userId);
            throw new WalletException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found for user.");
        }

        return WalletResponse.fromWallet(walletRepository.findByUserId(userId).get());
    }

    @Override
    @Transactional(readOnly = true)
    public Wallet getActiveWalletByUserId(UUID userId) {
        return walletRepository
                .findByUserIdAndStatus(userId, WalletStatus.ACTIVE)
                .orElseThrow(() -> {
                    // WHY check if wallet exists to give better error?
                    // If wallet exists but inactive → WALLET_INACTIVE
                    // If wallet doesn't exist at all → WALLET_NOT_FOUND
                    // Same orElseThrow but better message for client.
                    boolean exists = walletRepository
                            .findByUserId(userId).isPresent();
                    return exists
                            ? new WalletException(ErrorCode.WALLET_INACTIVE)
                            : new WalletException(ErrorCode.WALLET_NOT_FOUND);
                });

    }

    @Override
    public void credit(UUID walletId, BigDecimal amount) {
        validate(amount);
        int rowsAffected = walletRepository.creditBalance(walletId, amount);
        if(rowsAffected == 0){
          walletRepository.findById(walletId)
                  .ifPresentOrElse(
                          (wallet)->{
                      throw new WalletException(ErrorCode.WALLET_INACTIVE,"Credit Wallet in not Active");
                  },
                          ()->{
                   throw new WalletException(ErrorCode.WALLET_NOT_FOUND);
                  });
        }
    }

    private void validate(BigDecimal amount) {
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new WalletException(ErrorCode.INVALID_AMOUNT);
    }

    @Override
    public void debit(UUID walletId, BigDecimal amount) {
        validate(amount);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletException(ErrorCode.WALLET_NOT_FOUND,
                                "Wallet not found: " + walletId));

        // Now check status separately
        if (wallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new WalletException(ErrorCode.WALLET_INACTIVE,
                    "Wallet is not active: " + wallet.getWalletStatus());
        }
        int rowsAffected = walletRepository.debitBalance(walletId, amount);
        if(rowsAffected == 0){
           throw new WalletException(ErrorCode.INSUFFICIENT_FUNDS);
        }
    }

    @Override
    public WalletResponse freezeWallet(UUID userId) {
           Wallet wallet = walletRepository.findByUserId(userId).get();
           if(wallet.getWalletStatus()!= WalletStatus.FREEZED) {
               log.info("Wallet freezing for user Id {}",userId);
                    wallet.setWalletStatus(WalletStatus.FREEZED);
                    Wallet updatedWallet = walletRepository.save(wallet);
                    log.info("Success : Wallet freezing for user Id {}",userId);
                    return WalletResponse.fromWallet(updatedWallet);
           }else{
               log.info("Wallet already frozen for user Id {}",userId);
               throw new WalletException(ErrorCode.WALLET_INACTIVE,"Wallet already freezed");
           }
    }
}
