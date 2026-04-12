package com.example.digitalwallet.payment.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GatewayorderResult {

    private String gatewayOrderId;
    private long amountInPaisa;
    private String currency;
    private String status;
}
