package com.example.digitalwallet.wallet.model;

import com.example.digitalwallet.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "wallet_user_id",
                        columnNames = "user_id"
                )})
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wallet_id", updatable = false, nullable = false)
    private UUID id;


    //preciosion upto scale 4 - more accuracy
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;


    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_status", updatable = true, nullable = false)
    @Builder.Default
    private  WalletStatus walletStatus = WalletStatus.ACTIVE;


    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency ="INR";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
