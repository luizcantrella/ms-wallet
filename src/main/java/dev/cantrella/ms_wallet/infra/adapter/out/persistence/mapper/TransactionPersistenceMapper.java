package dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper;

import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {
    public TransactionEntity toEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionEntity.builder()
                .id(transaction.getId() != null ? transaction.getId() : null)
                .sourceWalletId(transaction.getSourceWalletId())
                .destinationWalletId(transaction.getDestinationWalletId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Transaction.reconstruct(
                entity.getId(),
                entity.getSourceWalletId(),
                entity.getDestinationWalletId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTimestamp());
    }
}
