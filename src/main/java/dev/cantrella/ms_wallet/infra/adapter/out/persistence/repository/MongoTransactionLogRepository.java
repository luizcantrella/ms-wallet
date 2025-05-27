package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionMongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MongoTransactionLogRepository extends MongoRepository<TransactionMongoEntity, String> {

    @Query("{ '$and': [ { '$or': [ { 'source_wallet_id': ?0 }, { 'destination_wallet_id': ?0 } ] }, { 'timestamp': { '$lte': ?1 } } ] }")
    List<TransactionMongoEntity> findByWalletIdAndTimestampUntil(String walletId, long timestamp);

}
