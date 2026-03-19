package com.example.digitalwallet.transaction.model;

import com.example.digitalwallet.wallet.model.Wallet;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
//Indexing on referenceID (idempotency check is done often),walletID(history we check often)
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transactions_wallet_id",
                        columnList = "wallet_id"),
                @Index(name = "idx_transactions_reference_id",
                        columnList = "reference_id"),
                @Index(name = "idx_transactions_created_at",
                        columnList = "created_at")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name = "id",nullable = false,updatable = false)
    private UUID id;

    //own wallet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id",nullable = false,updatable = false)
    private Wallet wallet;
    //amount
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    //type
    @Enumerated(EnumType.STRING)
    @Column( nullable = false , updatable = false)
    private TransactionType transactionType ;

    //Status
    @Builder.Default
    @Column(nullable = false , updatable = false)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

    //related wallet
    @Column(name = "related_wallet_id")
    private UUID relatedWalletId;
    //balace before
    @Column(name = "balance_before" , nullable = false , precision = 19, scale = 4)
    private BigDecimal balanceBefore ;

    //balance after
    @Column(name = "balance_after" , nullable = false , precision = 19, scale = 4)
    private BigDecimal balanceAfter ;
    //referenceID (Idempotency)

    @Column(name = "reference_id",
            nullable = false,
            unique = true,
            length = 100)
    private String referenceId;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency ="INR";



    @CreationTimestamp
    @Column(name = "created_at",
            updatable = false,
            nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            nullable = false)
    private LocalDateTime updatedAt;
}
