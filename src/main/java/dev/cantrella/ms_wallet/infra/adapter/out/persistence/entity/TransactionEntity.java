package dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity;

import dev.cantrella.ms_wallet.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "source_wallet_id", nullable = false)
    private UUID sourceWalletId;

    @Column(name = "destination_wallet_id")
    private UUID destinationWalletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(6) WITHOUT TIME ZONE")
    private LocalDateTime timestamp;
}