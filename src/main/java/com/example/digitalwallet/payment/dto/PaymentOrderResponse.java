package com.example.digitalwallet.payment.dto;

import com.example.digitalwallet.payment.model.PaymentOrder;
import com.example.digitalwallet.payment.model.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentOrderResponse {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String failureReason;
    private LocalDateTime createdAt;

    public static PaymentOrderResponse fromPaymentOrder(
            PaymentOrder order) {
        return PaymentOrderResponse.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .gatewayOrderId(order.getGatewayOrderId())
                .gatewayPaymentId(
                        order.getGatewayPaymentId())
                .failureReason(order.getFailureReason())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
