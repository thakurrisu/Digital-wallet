package com.example.digitalwallet.wallet.repo;

import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.model.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByIdAndWalletStatus(UUID id, WalletStatus walletStatus);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount " +
            "WHERE w.id = :id " +
            "AND w.walletStatus = 'ACTIVE'")
    int creditBalance(@Param("id") UUID id,
                      @Param("amount") BigDecimal amount);
    // WHY return int?
    // Returns number of rows affected.
    // 1 = success
    // 0 = wallet not found OR not ACTIVE
    // Caller checks return value → throws exception if 0.
    // Single atomic SQL — no read before write.
    // DB handles concurrent writes at row level.



    //Check balance and atomicity is maintened if two threads acces it will be debit safe.
    //only one sql operation - so atomic.
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount " +
            "WHERE w.id = :id " +
            "AND w.balance >= :amount " +
            "AND w.walletStatus = 'ACTIVE'")
    int debitBalance(@Param("id") UUID id,
                     @Param("amount") BigDecimal amount);


    @Modifying
    @Query("UPDATE Wallet w SET w.walletStatus = :status WHERE w.user.id = :userid")
    void updateWalletStatus(@Param("userid") UUID id, @Param("status") WalletStatus status);
}