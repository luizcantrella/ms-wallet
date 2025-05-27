package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.application.port.CreateWalletUseCase;
import dev.cantrella.ms_wallet.application.dto.CreateWalletCommand;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    @Override
    @Transactional
    public UUID execute(CreateWalletCommand createWalletCommand) {

        // validate if user has wallet
        if( walletRepositoryPort.existsByUserId(createWalletCommand.userId()) ) {
            throw new RuntimeException("Wallet already exists for this userId");
        }
        // create new wallet
        Wallet wallet = Wallet.create(createWalletCommand.userId());

        // register new wallet
        wallet = walletRepositoryPort.save(wallet);
        return wallet.getId();
    }
}
