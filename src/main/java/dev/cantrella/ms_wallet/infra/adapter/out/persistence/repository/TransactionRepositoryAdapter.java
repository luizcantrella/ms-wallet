package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionEntity;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final JpaTransactionRepository repository;
    private final TransactionPersistenceMapper mapper;
    @Override
    public void save(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        TransactionEntity entity = mapper.toEntity(transaction);
        repository.save(entity);
    }
}
