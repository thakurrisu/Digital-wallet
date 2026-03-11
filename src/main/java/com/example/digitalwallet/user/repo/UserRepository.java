package com.example.digitalwallet.user.repo;

import com.example.digitalwallet.user.model.User;
import com.example.digitalwallet.user.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User , UUID> {

    Optional<User>  findByEmailAndIsDeletedFalse(String email);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    @Modifying
    @Query("UPDATE User u SET u.status = :status, " +
            "u.isDeleted = true, " +
            "u.deletedAt = :deletedAt " +
            "WHERE u.id = :id")
    void softDeleteUser(@Param("id") UUID id,
                        @Param("status") UserStatus status,
                        @Param("deletedAt") LocalDateTime deletedAt);


}
