package com.example.digitalwallet.payment.service;

import com.example.digitalwallet.common.exception.ErrorCode;
import com.example.digitalwallet.common.exception.WalletException;
import com.example.digitalwallet.payment.dto.InitiatePaymentRequest;
import com.example.digitalwallet.payment.dto.InitiatePaymentResponse;
import com.example.digitalwallet.payment.dto.PaymentOrderResponse;
import com.example.digitalwallet.payment.gateway.PaymentGateway;
import com.example.digitalwallet.payment.model.GatewayorderResult;
import com.example.digitalwallet.payment.model.PaymentOrder;
import com.example.digitalwallet.payment.model.PaymentStatus;
import com.example.digitalwallet.payment.repo.PaymentRepository;
import com.example.digitalwallet.transaction.dto.DepositRequest;
import com.example.digitalwallet.transaction.model.Transaction;
import com.example.digitalwallet.transaction.service.TransactionService;
import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.repo.UserRepository;
import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.service.WalletService;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaymentServiceImp implements PaymentService{


    @Value("${razorpay.key-id}")
    private String razorpayKeyId;
    //gateway,walletservice,userRepo,transac
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;

    public PaymentServiceImp(PaymentRepository paymentRepository, PaymentGateway paymentGateway, UserRepository userRepository, WalletService walletService, TransactionService transactionService) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @Override
    public InitiatePaymentResponse initiatePayment(UUID userId, InitiatePaymentRequest request) {
        log.info("Initiating payment for userId={}, amount={}", userId, request.getAmount());

        User user = this.userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> {
                    log.warn("Payment initiation failed - user not found. userId={}", userId);
                    return new WalletException(ErrorCode.USER_NOT_FOUND);
                });
        Wallet wallet = this.walletService.getActiveWalletByUserId(userId);
        log.debug("Resolved active wallet walletId={} for userId={}", wallet.getId(), userId);

        String recipt =  userId.toString()
                .replace("-", "")
                .substring(0, 8)
                + "_" + System.currentTimeMillis();
        log.debug("Creating gateway order with receipt={}, amount={}, currency=INR", recipt, request.getAmount());

        GatewayorderResult gatewayorderResult = this.paymentGateway.createOrder(request.getAmount(), "INR", recipt);
        log.info("Gateway order created successfully. gatewayOrderId={}, userId={}",
                gatewayorderResult.getGatewayOrderId(), userId);

        PaymentOrder paymentOrder = PaymentOrder.builder()
                .gatewayOrderId(gatewayorderResult.getGatewayOrderId())
                .amount(request.getAmount())
                .user(user)
                .wallet(wallet)
                .build();
        PaymentOrder savedPaymentOrder = this.paymentRepository.save(paymentOrder);
        log.info("Payment order persisted. paymentOrderId={}, gatewayOrderId={}, userId={}, amount={}",
                savedPaymentOrder.getId(), savedPaymentOrder.getGatewayOrderId(), userId, savedPaymentOrder.getAmount());

        return InitiatePaymentResponse.builder().paymentOrderId(savedPaymentOrder.getId())
                .amount(savedPaymentOrder.getAmount())
                .gatewayOrderId(paymentOrder.getGatewayOrderId())
                .keyId(razorpayKeyId)
                .currency("INR")
                .amount(paymentOrder.getAmount())
                .build();
    }

    @Override
    public void handleCallBack(String payload, String razorpaySignature) {
        log.info("Received Razorpay webhook callback. payloadSize={} bytes", payload == null ? 0 : payload.length());

        boolean isValid = paymentGateway.verifyWebhookSignature(payload, razorpaySignature);
        if (!isValid) {
            log.error("Webhook signature verification failed. Rejecting callback.");
            throw new WalletException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        log.debug("Webhook signature verified successfully");

        JSONObject jsonPayload = new JSONObject(payload);
        String event = jsonPayload.getString("event");
        log.info("Processing webhook event={}", event);

        switch (event) {
            case "payment.captured" -> handleCapturedPayment(jsonPayload);
            case "payment.failed" -> handleFailedPayment(jsonPayload);
            default -> log.warn("Unhandled webhook event received. event={}", event);
        }
    }

    private void handleCapturedPayment(JSONObject jsonPayload) {
        JSONObject paymentEntity = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

        String gatewayOrderId = paymentEntity.getString("order_id");
        String paymentId = paymentEntity.getString("id");
        log.info("Handling captured payment. gatewayOrderId={}, gatewayPaymentId={}", gatewayOrderId, paymentId);

        PaymentOrder paymentOrder = paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() -> {
                    log.error("Payment order not found for captured webhook. gatewayOrderId={}", gatewayOrderId);
                    return new WalletException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        if (paymentOrder.getStatus() != PaymentStatus.PENDING) {
            log.warn("Captured webhook ignored - payment already processed. paymentOrderId={}, currentStatus={}",
                    paymentOrder.getId(), paymentOrder.getStatus());
            return;
        }

        log.debug("Crediting wallet via deposit. paymentOrderId={}, userId={}, amount={}",
                paymentOrder.getId(), paymentOrder.getUser().getId(), paymentOrder.getAmount());
        DepositRequest dpr = new DepositRequest(paymentOrder.getAmount(), paymentId);
        transactionService.deposit(paymentOrder.getUser().getId(), dpr);

        paymentOrder.setStatus(PaymentStatus.SUCCESS);
        paymentOrder.setGatewayPaymentId(paymentId);
        paymentRepository.save(paymentOrder);
        log.info("Payment captured and wallet credited successfully. paymentOrderId={}, gatewayPaymentId={}, userId={}, amount={}",
                paymentOrder.getId(), paymentId, paymentOrder.getUser().getId(), paymentOrder.getAmount());
    }

    private void handleFailedPayment(JSONObject jsonPayload) {
        JSONObject paymentEntity = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

        String gatewayOrderId = paymentEntity.getString("order_id");
        String failureReason = paymentEntity.optString("failure_reason","Payemnt Failed");
        String paymentId = paymentEntity.getString("id");
        PaymentOrder paymentOrder = paymentRepository.findByGatewayOrderId(gatewayOrderId)
                                                     .orElseThrow(()->new WalletException(ErrorCode.PAYMENT_NOT_FOUND));
        if(paymentOrder.getStatus() != PaymentStatus.PENDING){
            log.info("Payemnt already Processed");
            return;
        }

        paymentOrder.setStatus(PaymentStatus.FAILED);
        paymentOrder.setFailureReason(failureReason);
        paymentOrder.setGatewayPaymentId(paymentId);
        paymentRepository.save(paymentOrder);

    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderResponse getPaymentStatus(UUID paymentOrderId, UUID userId) {
        Wallet wallet = this.walletService.getActiveWalletByUserId(userId);
        PaymentOrder paymentOrder = paymentRepository
                .findById(paymentOrderId)
                .filter(order -> order.getUser().getId().equals(userId))
                .orElseThrow(()-> new WalletException(ErrorCode.PAYMENT_NOT_FOUND ,"Payment not found for order Id" + paymentOrderId));
        return PaymentOrderResponse.fromPaymentOrder(paymentOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderResponse> getPaymentHistory(UUID userId) {
        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(PaymentOrderResponse::fromPaymentOrder)
                .collect(Collectors.toList());
    }
}
