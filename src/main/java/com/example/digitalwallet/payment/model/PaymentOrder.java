package com.example.digitalwallet.payment.model;

import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.GeneratorStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "payment_orders",
        indexes = {
                @Index(
                        name = "idx_payment_orders_gateway_order_id",
                        columnList = "gateway_order_id"
                ),
                @Index(
                        name = "idx_payment_orders_user_id",
                        columnList = "user_id"
                )
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name ="payment_id",updatable = false,nullable = false)
    private UUID Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Column(name = "gateway",nullable = false)
    @Builder.Default
    private String gatewayName ="Razorpay";

    @Column(name = "gateway_order_id",nullable = false,unique = true  )
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "currency",nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false,precision = 19,scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name ="failure_reason",length=255)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
