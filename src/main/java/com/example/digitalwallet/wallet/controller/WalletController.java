package com.example.digitalwallet.wallet.controller;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.wallet.dto.WalletResponse;
import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.service.WalletServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/v1/wallet")
public class WalletController {

    WalletServiceImpl walletService;

    public WalletController(WalletServiceImpl walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@AuthenticationPrincipal UUID userId){
        WalletResponse walletResponse = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(walletResponse));
    }

    //Post - Credit ("/v1/wallet/credit)
    @PostMapping("/v1/wallet/credit")
    public ResponseEntity<ApiResponse<WalletResponse>> credit(@AuthenticationPrincipal UUID userId , @RequestBody BigDecimal amount){
            return ResponseEntity.ok(ApiResponse.ok(walletService.withdraw(userId,amount)));
    }

    //Post - Debit ("/v1/wallet/debit)
    @PostMapping("/v1/wallet/debit")
    public ResponseEntity<ApiResponse<WalletResponse>> debit(@AuthenticationPrincipal UUID userId , @RequestBody BigDecimal amount){
        return ResponseEntity.ok(ApiResponse.ok(walletService.deposit(userId,amount)));
    }

    //path - Freeze("/v1/wallet/freeze)
    @PatchMapping("/v1/wallet/freeze")
    public ResponseEntity<ApiResponse<WalletResponse>> freeze(@AuthenticationPrincipal UUID userId){
        return ResponseEntity.ok(ApiResponse.ok(walletService.freezeWallet(userId)));
    }

}
