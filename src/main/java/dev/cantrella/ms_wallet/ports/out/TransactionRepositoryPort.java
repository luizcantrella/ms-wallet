package dev.cantrella.ms_wallet.ports.out;

import dev.cantrella.ms_wallet.domain.Transaction;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);
}
