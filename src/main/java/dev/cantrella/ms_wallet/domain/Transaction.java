package dev.cantrella.ms_wallet.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Getter
public class Transaction {

    private UUID id;
    private UUID sourceWalletId;
    private UUID destinationWalletId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    private Transaction (
            UUID sourceWalletId,
            UUID destinationWalletId,
            TransactionType type,
            BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.sourceWalletId = Objects.requireNonNull(sourceWalletId, "Source wallet cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.amount = validateAmount(amount);
        this.destinationWalletId = destinationWalletId;
        this.timestamp = LocalDateTime.now();
    }

    private Transaction (
            UUID id,
            UUID sourceWalletId,
            UUID destinationWalletId,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime timestamp) {
        this.id = id;
        this.sourceWalletId = sourceWalletId;
        this.type = type;
        this.amount = amount;
        this.destinationWalletId = destinationWalletId;
        this.timestamp = timestamp;
    }

    public static Transaction reconstruct(
            UUID id,
            UUID sourceWalletId,
            UUID destinationWalletId,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime timestamp) {
        return new Transaction(id, sourceWalletId, destinationWalletId, type, amount, timestamp);
    }

    public static Transaction createTransfer(UUID source, UUID destination, BigDecimal amount) {
        return new Transaction(
                source,
                Objects.requireNonNull(destination, "Source wallet cannot be null"),
                TransactionType.TRANSFER,
                amount);
    }

    public static Transaction createDeposit(UUID source, BigDecimal amount) {
        return new Transaction(source, null, TransactionType.DEPOSIT, amount);
    }

    public static Transaction createWithdraw(UUID source, BigDecimal amount) {
        return new Transaction(source, null, TransactionType.WITHDRAW, amount);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            //throw new InvalidTransactionException("Amount must be positive");
            throw new RuntimeException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            //throw new InvalidTransactionException("Amount cannot have more than 2 decimal places");
            throw new RuntimeException("Amount cannot have more than 2 decimal places");
        }
        return amount;
    }

}
