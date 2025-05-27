package dev.cantrella.ms_wallet.ports.out;

import dev.cantrella.ms_wallet.domain.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepositoryPort {

    boolean existsByUserId(String userId);
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
}
