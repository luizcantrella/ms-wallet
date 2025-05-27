package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.domain.Transaction;
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
    public Transaction save(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        TransactionEntity entity = mapper.toEntity(transaction);
        TransactionEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
