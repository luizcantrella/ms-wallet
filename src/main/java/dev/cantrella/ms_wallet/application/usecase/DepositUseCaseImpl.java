package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.DepositCommand;
import dev.cantrella.ms_wallet.application.port.DepositUseCase;
import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositUseCaseImpl implements DepositUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CachePort cachePort;

    @Override
    @Transactional
    public Transaction execute(DepositCommand command) {
        Wallet wallet = walletRepositoryPort
                .findById(command.walletId())
                .orElseThrow(()-> new RuntimeException("WalletNotFound"));
        wallet.deposit(command.amount());

        Transaction transaction = Transaction.createDeposit(command.walletId(),command.amount(), "BLR");

        walletRepositoryPort.save(wallet);
        cachePort.evict(wallet.getId().toString());
        return transactionRepositoryPort.save(transaction);
    }
}
