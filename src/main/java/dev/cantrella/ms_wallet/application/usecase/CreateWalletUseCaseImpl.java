package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.application.port.CreateWalletUseCase;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    @Override
    @Transactional
    public UUID execute(String userEmail) {
        Objects.requireNonNull(userEmail, "UserEmail can not be null");
        if( walletRepositoryPort.existsByUserId(userEmail) ) {
            throw new RuntimeException("Wallet already exists for this user");
        }
        Wallet wallet = Wallet.create(userEmail);
        wallet = walletRepositoryPort.save(wallet);
        return wallet.getId();

    }
}
