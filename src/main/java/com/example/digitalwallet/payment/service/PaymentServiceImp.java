package com.example.digitalwallet.payment.service;

import com.example.digitalwallet.payment.dto.InitiatePaymentRequest;
import com.example.digitalwallet.payment.dto.InitiatePaymentResponse;
import com.example.digitalwallet.payment.dto.PaymentOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImp implements PaymentService{
    @Override
    public InitiatePaymentResponse initiatePayment(UUID userId, InitiatePaymentRequest request) {
        return null;
    }

    @Override
    public void handleCallBack(String payload, String razorpaySignature) {

    }

    @Override
    public PaymentOrderResponse getPaymentStatus(UUID paymentOrderId, UUID userId) {
        return null;
    }

    @Override
    public List<PaymentOrderResponse> getPaymentHistory(UUID userId) {
        return List.of();
    }
}
