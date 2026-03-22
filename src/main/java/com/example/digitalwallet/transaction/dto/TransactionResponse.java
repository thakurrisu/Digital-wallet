package com.example.digitalwallet.transaction.dto;

import com.example.digitalwallet.transaction.model.Transaction;
import com.example.digitalwallet.transaction.model.TransactionStatus;
import com.example.digitalwallet.transaction.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransactionResponse {

    private UUID id;

    private UUID walletId;
    // WHY walletId not full wallet object?
    // Client already knows their walletId.
    // Embedding full wallet = unnecessary data.
    // Keep responses lean — only what client needs.

    private TransactionType type;
    // DEPOSIT / WITHDRAW / TRANSFER_IN / TRANSFER_OUT
    // Client uses this to show correct icon/label in UI:
    // DEPOSIT     → green arrow down  ↓
    // WITHDRAW    → red arrow up      ↑
    // TRANSFER_IN → green arrow       →
    // TRANSFER_OUT → red arrow        ←

    private BigDecimal amount;

    private String currency;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    // WHY expose both snapshots?
    // Client can show: "Balance: ₹1000 → ₹500"
    // Useful for transaction detail screen
    // Makes every transaction self-explanatory
    // No need to calculate from history

    private TransactionStatus status;
    // Client shows appropriate UI:
    // PENDING → spinner
    // SUCCESS → checkmark
    // FAILED  → error icon
    // REVERSED → refund badge

    private String referenceId;
    // WHY return referenceId?
    // Client can verify their sent key was received.
    // Useful for debugging duplicate detection.
    // "Server returned same referenceId = duplicate detected"

    private String description;
    // Human readable:
    // "Deposit via UPI"
    // "Transfer to wallet xyz"
    // Shown in transaction history UI

    private UUID relatedWalletId;
    // NULL for DEPOSIT / WITHDRAW
    // For TRANSFER: shows the other party's walletId
    // Client can display: "Sent to: xyz-wallet"
    // or "Received from: abc-wallet"

    private LocalDateTime createdAt;

    // ================================================================
    // STATIC FACTORY METHOD
    // ================================================================

    public static TransactionResponse fromTransaction(
            Transaction transaction) {

        return TransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                // WHY transaction.getWallet().getId()?
                // Transaction has Wallet object (LAZY loaded).
                // We only access .getId() here.
                // getId() on a LAZY proxy does NOT
                // trigger a database query.
                // Hibernate optimizes this — id is always
                // available without loading the full object.
                // Safe to call outside @Transactional.
                .type(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getTransactionStatus())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .relatedWalletId(transaction.getRelatedWalletId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}