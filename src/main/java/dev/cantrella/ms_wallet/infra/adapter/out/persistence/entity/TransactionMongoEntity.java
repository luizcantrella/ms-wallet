package dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;
import java.math.BigDecimal;

@Document(collection = "transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMongoEntity {
    @MongoId
    @Field("_id")
    private String id;

    @Field("id")
    private String transactionId;
    @Field("source_wallet_id")
    private String sourceWalletId;
    @Field("destination_wallet_id")
    private String destinationWalletId;
    @Field("type")
    private String type;
    @Field("amount")
    private BigDecimal amount;
    @Field("timestamp")
    private Long timestamp;

}