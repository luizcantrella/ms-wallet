package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.DepositOrWithdrawCommand;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.application.port.WithdrawUseCase;
import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawUseCaseImpl implements WithdrawUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final CachePort cachePort;

    @Override
    @Transactional
    public Transaction execute(DepositOrWithdrawCommand command) {
        Wallet wallet = walletRepositoryPort
                .findByUserIdForUpdate(command.userId())
                .orElseThrow(()-> new WalletNotFoundException(command.userId()));
        Transaction transaction = Transaction.createWithdraw(wallet.getId(),command.amount());
        wallet.withdraw(command.amount());
        walletRepositoryPort.update(wallet);
        cachePort.evict(command.userId());
        transactionRepositoryPort.save(transaction);
        return transaction;
    }
}
