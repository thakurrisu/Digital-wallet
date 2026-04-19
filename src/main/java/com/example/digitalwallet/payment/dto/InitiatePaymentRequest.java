package com.example.digitalwallet.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class InitiatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "1.00",
            message = "Minimum payment amount is ₹1"
    )
    private BigDecimal amount;
}


