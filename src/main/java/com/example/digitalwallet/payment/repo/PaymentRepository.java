package com.example.digitalwallet.payment.repo;

import com.example.digitalwallet.payment.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentOrder, UUID> {

    
    Optional<PaymentOrder> findByGatewayOrderId(String gatewayOrderId);

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

