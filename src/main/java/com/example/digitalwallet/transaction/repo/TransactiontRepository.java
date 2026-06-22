package com.example.digitalwallet.transaction.repo;

import com.example.digitalwallet.transaction.model.Transaction;

import com.example.digitalwallet.transaction.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactiontRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceId(String referenceId);

    boolean existsByReferenceId(String referenceId);

    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId , Pageable pageable);

    Optional<Transaction> findByIdAndWalletId(UUID id , UUID walletId);

    @Modifying
    @Query("UPDATE Transaction  t SET t.transactionStatus = :status where t.id= :id")
    void updateTransactionStatus(@Param("id")  UUID id, @Param("status")TransactionStatus status);
}
