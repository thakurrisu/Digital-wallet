package com.example.digitalwallet.payment.gateway;

import com.example.digitalwallet.common.exception.ErrorCode;
import com.example.digitalwallet.common.exception.WalletException;
import com.example.digitalwallet.payment.model.GatewayPaymentResult;
import com.example.digitalwallet.payment.model.GatewayorderResult;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;
    private final String webHookSecret;

    public RazorpayPaymentGateway(@Value("${razorpay.key-id}") String keyId , @Value("${razorpay.key-secret}") String keySecret , @Value("${razorpay.webhook-secret}") String webhookSecret){

        try {
            this.razorpayClient = new RazorpayClient(keyId,keySecret);
            this.webHookSecret = webhookSecret;

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to initialize Razorpay Client",e);
        }
    }

    @Override
    public GatewayorderResult createOrder(BigDecimal amount, String currency, String receipt) {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",amount);
        orderRequest.put("currency",currency);
        orderRequest.put("receipt",receipt);
        orderRequest.put("payment_capture",1); // 1 - auto Capture
        try {
           Order order =  razorpayClient.orders.create(orderRequest);
           return  GatewayorderResult.builder().gatewayOrderId(order.get("id"))
                   .currency(order.get("currency"))
                   .amountInPaisa(Long.valueOf(
                           order.get("amount").toString()))
                   .status(order.get("status"))
                   .build();
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}",
                    e.getMessage());
            throw new WalletException(
                    ErrorCode.PAYMENT_FAILED,
                    "Payment gateway error: " + e.getMessage());
        }

    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
           return com.razorpay.Utils.verifyWebhookSignature(payload,signature,webHookSecret);
        } catch (RazorpayException e) {
           log.info("Razorpay webhook signature verification failed: {}",e.getMessage());
           return false;
        }
    }

    @Override
    public GatewayPaymentResult fetchPaymentResult(String gatewayPaymentId) {

        try {
            Payment payment = razorpayClient.payments
                    .fetch(gatewayPaymentId);

            return GatewayPaymentResult.builder()
                    .gatewayPaymentId(
                            payment.get("id"))
                    .gatwayOrderId(
                            payment.get("order_id"))
                    .status(payment.get("status"))
                    .failureReason(
                            payment.has("error_description")
                                    ? payment.get("error_description")
                                    : null)
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to fetch payment: {}",
                    e.getMessage());
            throw new WalletException(
                    ErrorCode.PAYMENT_NOT_FOUND,
                    "Payment not found: " + gatewayPaymentId);
        }
    }

}
