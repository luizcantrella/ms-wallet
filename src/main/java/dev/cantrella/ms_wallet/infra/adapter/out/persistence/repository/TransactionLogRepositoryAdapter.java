package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionMongoEntity;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper.TransactionLogPersistenceMapper;
import dev.cantrella.ms_wallet.ports.out.TransactionLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TransactionLogRepositoryAdapter implements TransactionLogRepositoryPort {

    private final MongoTransactionLogRepository repository;
    private final TransactionLogPersistenceMapper mapper;
    @Override
    public List<Transaction> listByWalletId(UUID walletId, LocalDateTime timestamp) {
        List<TransactionMongoEntity> transactionsLogEntities = repository
                .findByWalletIdAndTimestampUntil(
                        walletId.toString(),
                        timestamp.atZone(ZoneOffset.UTC).toInstant().getEpochSecond() * 1000
                        );
        return transactionsLogEntities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
