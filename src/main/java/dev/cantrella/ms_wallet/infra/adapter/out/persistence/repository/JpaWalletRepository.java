package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaWalletRepository extends JpaRepository<WalletEntity, UUID> {
    boolean existsByUserId(String userId);
    @Query(value = "SELECT * FROM wallets w WHERE w.id = :id FOR UPDATE", nativeQuery = true)
    Optional<WalletEntity> findByIdWithPessimisticLock(@Param("id") UUID id);

    @Query(value = "SELECT * FROM wallets w WHERE w.user_id = :userId FOR UPDATE", nativeQuery = true)
    Optional<WalletEntity> findByUserIdWithPessimisticLock(@Param("userId")String userId);

    Optional<WalletEntity> findByUserId(String userId);
}
