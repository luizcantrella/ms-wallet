package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaWalletRepository extends JpaRepository<WalletEntity, UUID> {
    boolean existsByUserId(String userId);
}
