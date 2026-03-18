package com.example.digitalwallet.wallet.dto;

import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.model.WalletStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WalletResponse {

    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private LocalDateTime createdAt;


    public static WalletResponse fromWallet(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getWalletStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}