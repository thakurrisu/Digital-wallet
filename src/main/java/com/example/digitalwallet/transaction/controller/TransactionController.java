package com.example.digitalwallet.transaction.controller;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.transaction.dto.DepositRequest;
import com.example.digitalwallet.transaction.dto.TransactionResponse;
import com.example.digitalwallet.transaction.dto.TransferRequest;
import com.example.digitalwallet.transaction.dto.WithdrawRequest;
import com.example.digitalwallet.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@AuthenticationPrincipal UUID userId, @RequestBody @Valid DepositRequest depReq) {
        TransactionResponse transactionResponse = this.transactionService.deposit(userId, depReq);
        return ResponseEntity.ok(ApiResponse.ok("Deposit completed successfully", transactionResponse));
    }

    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<TransactionResponse>> credit(@AuthenticationPrincipal UUID userId, @RequestBody @Valid WithdrawRequest creditReq) {
        TransactionResponse transactionResponse = this.transactionService.withdraw(userId, creditReq);
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal completed successfully", transactionResponse));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@AuthenticationPrincipal UUID userId, @RequestBody @Valid TransferRequest transferRequest) {
        TransactionResponse transactionResponse = this.transactionService.transfer(userId, transferRequest);
        return ResponseEntity.ok(ApiResponse.ok("Transfer completed successfully", transactionResponse));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@AuthenticationPrincipal UUID userId, @PathVariable UUID transactionId) {
        TransactionResponse transactionResponse = this.transactionService.getTransactionById(userId, transactionId);
        return ResponseEntity.ok(ApiResponse.ok("Transaction retrieved successfully", transactionResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(@AuthenticationPrincipal UUID userId, Pageable pageable) {
        Page<TransactionResponse> transactions = this.transactionService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Transaction history retrieved successfully", transactions));
    }
}
