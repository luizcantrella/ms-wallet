package dev.cantrella.ms_wallet.ports.out;

import dev.cantrella.ms_wallet.domain.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionLogRepositoryPort {

    List<Transaction> listByWalletId(UUID walletId, LocalDateTime timestamp);
}
