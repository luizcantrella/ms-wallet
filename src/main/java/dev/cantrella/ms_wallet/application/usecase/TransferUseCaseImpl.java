package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.TransferCommand;
import dev.cantrella.ms_wallet.application.exception.OperationNotAllowedException;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.application.port.TransferUseCase;
import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferUseCaseImpl implements TransferUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CachePort cachePort;

    @Override
    @Transactional
    public Transaction execute(TransferCommand command) {
        log.info("Transferring {} from wallet {} to wallet {}", command.amount(),
                command.sourceWalletId(), command.destinationWalletId());
        Wallet sourceWallet = walletRepositoryPort
                .findByIdForUpdate(command.sourceWalletId())
                .orElseThrow(()-> new WalletNotFoundException(command.sourceWalletId()));
        Wallet destinationWallet = walletRepositoryPort
                .findByIdForUpdate(command.destinationWalletId())
                .orElseThrow(()-> new WalletNotFoundException(command.destinationWalletId()));
        if(sourceWallet.equals(destinationWallet)) {
            throw new OperationNotAllowedException("Source wallet and destination wallet is same");
        }

        Transaction transaction = Transaction.createTransfer(
                command.sourceWalletId(),
                command.destinationWalletId(),
                command.amount());
        sourceWallet.withdraw(command.amount());
        destinationWallet.deposit(command.amount());
        walletRepositoryPort.update(sourceWallet);
        walletRepositoryPort.update(destinationWallet);
        cachePort.evict(sourceWallet.getUserId());
        cachePort.evict(destinationWallet.getUserId());
        log.info("Transaction completed with transaction id: {}", transaction.getId());
        transactionRepositoryPort.save(transaction);
        return transaction;
    }
}
