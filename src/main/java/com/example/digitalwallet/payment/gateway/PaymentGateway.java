package com.example.digitalwallet.payment.gateway;

import com.example.digitalwallet.payment.model.GatewayPaymentResult;
import com.example.digitalwallet.payment.model.GatewayorderResult;

import java.math.BigDecimal;

public interface PaymentGateway {

    //createOrder
    GatewayorderResult createOrder(BigDecimal amount, String currency,String receipt);

    //ValidateSignature
    boolean verifyWebhookSignature(String payload , String signature);

    //FetchPayment
    GatewayPaymentResult fetchPaymentResult(String paymentId);
}
