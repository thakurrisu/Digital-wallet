package com.example.digitalwallet.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class InitiatePaymentResponse {
    private UUID paymentOrderId;

    private String gatewayOrderId;

    private String keyId;

    private BigDecimal amount;
    private String currency;
}
