package com.example.digitalwallet.payment.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayPaymentResult {

    private String gatewayPaymentId;
    private String gatwayOrderId;
    private String status;

    private String failureReason;
}
