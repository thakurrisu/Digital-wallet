package com.example.digitalwallet.payment.controller;

import com.example.digitalwallet.common.ApiResponse;
import com.example.digitalwallet.payment.dto.InitiatePaymentRequest;
import com.example.digitalwallet.payment.dto.InitiatePaymentResponse;
import com.example.digitalwallet.payment.dto.PaymentOrderResponse;
import com.example.digitalwallet.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    private ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(@AuthenticationPrincipal UUID userId, @RequestBody InitiatePaymentRequest paymentRequest){
            return ResponseEntity.ok(ApiResponse.ok("Payment Initiated", this.paymentService.initiatePayment(userId, paymentRequest)));
    }

    @GetMapping("/{paymentId}/status")
    private ResponseEntity<ApiResponse<PaymentOrderResponse>> getPaymentStatus(@AuthenticationPrincipal UUID userId,@PathVariable UUID paymentId){
        return ResponseEntity.ok(ApiResponse.ok(this.paymentService.getPaymentStatus(paymentId,userId)));
    }

    @GetMapping
    private ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> getPaymentHistory(@AuthenticationPrincipal UUID userId){
        return ResponseEntity.ok(ApiResponse.ok(this.paymentService.getPaymentHistory(userId)));
    }


    @PostMapping("/callback")
    private ResponseEntity<ApiResponse<Void>> callback( @RequestBody String payload,
                                                        @RequestHeader("X-Razorpay-Signature")
                                                        String razorpaySignature){
        //This will return to Razorpay client
        //RazorPay just need the ok response
        this.paymentService.handleCallBack(payload, razorpaySignature);
        return ResponseEntity.ok(ApiResponse.ok("Callback Processed"));
    }
}
