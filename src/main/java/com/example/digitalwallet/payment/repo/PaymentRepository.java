package com.example.digitalwallet.payment.repo;

import com.example.digitalwallet.payment.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentOrder, Long> {

    
    Optional<PaymentOrder> findByGatewayOrderId(String gatewayOrderId);

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

