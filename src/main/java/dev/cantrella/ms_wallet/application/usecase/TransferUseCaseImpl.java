package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.TransferCommand;
import dev.cantrella.ms_wallet.application.port.TransferUseCase;
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
public class TransferUseCaseImpl implements TransferUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CachePort cachePort;


    @Override
    @Transactional
    public Transaction execute(TransferCommand command) {
        Wallet sourceWallet = walletRepositoryPort
                .findByIdForUpdate(command.sourceWalletId())
                .orElseThrow(()-> new RuntimeException("WalletNotFound"));
        Wallet destinationWallet = walletRepositoryPort
                .findByIdForUpdate(command.destinationWalletId())
                .orElseThrow(()-> new RuntimeException("WalletNotFound"));
        sourceWallet.withdraw(command.amount());
        destinationWallet.deposit(command.amount());

        Transaction transaction = Transaction.createTransfer(
                command.sourceWalletId(),
                command.destinationWalletId(),
                command.amount());

        walletRepositoryPort.update(sourceWallet);
        walletRepositoryPort.update(destinationWallet);
        cachePort.evict(sourceWallet.getUserId());
        cachePort.evict(destinationWallet.getUserId());
        return transactionRepositoryPort.save(transaction);
    }
}
