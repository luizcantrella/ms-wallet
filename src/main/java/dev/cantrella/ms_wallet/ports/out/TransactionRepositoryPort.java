package dev.cantrella.ms_wallet.ports.out;

import dev.cantrella.ms_wallet.domain.model.Transaction;

public interface TransactionRepositoryPort {

    void save(Transaction transaction);
}
