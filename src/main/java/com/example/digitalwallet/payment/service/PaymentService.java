package com.example.digitalwallet.payment.service;

import com.example.digitalwallet.payment.dto.InitiatePaymentRequest;
import com.example.digitalwallet.payment.dto.InitiatePaymentResponse;
import com.example.digitalwallet.payment.dto.PaymentOrderResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    InitiatePaymentResponse initiatePayment(UUID userId,InitiatePaymentRequest request);

    void handleCallBack(String payload, String razorpaySignature);

    PaymentOrderResponse getPaymentStatus(UUID paymentOrderId, UUID userId);

    List<PaymentOrderResponse> getPaymentHistory(UUID userId);
}
