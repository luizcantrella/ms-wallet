package dev.cantrella.ms_wallet.ports.out;

import dev.cantrella.ms_wallet.domain.model.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepositoryPort {

    boolean existsByUserId(String userId);
    Wallet save(Wallet wallet);

    void update(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByUserId(String userId);
    Optional<Wallet> findByUserIdForUpdate(String userId);
    Optional<Wallet> findByIdForUpdate(UUID id);
}
