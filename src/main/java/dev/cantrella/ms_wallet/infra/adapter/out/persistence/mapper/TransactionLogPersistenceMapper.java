package dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper;

import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.domain.TransactionType;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionMongoEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class TransactionLogPersistenceMapper {
    public Transaction toDomain(TransactionMongoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Transaction.reconstruct(
                UUID.fromString(entity.getTransactionId()),
                UUID.fromString(entity.getSourceWalletId()),
                entity.getDestinationWalletId() != null ? UUID.fromString(entity.getDestinationWalletId()) : null,
                TransactionType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getCurrency(),
                Instant.ofEpochMilli(entity.getTimestamp())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                );
    }

//    private UUID convert(String source) {
//        if (source != null && !source.contains("-")) {
//            source = source.replaceFirst(
//                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
//                    "$1-$2-$3-$4-$5"
//            );
//        }
//        return UUID.fromString(source);
//    }
}
