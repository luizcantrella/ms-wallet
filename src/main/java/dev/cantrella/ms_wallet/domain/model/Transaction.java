package dev.cantrella.ms_wallet.domain.model;

import dev.cantrella.ms_wallet.domain.exception.InvalidAmountTransactionException;
import dev.cantrella.ms_wallet.domain.exception.NonNullValueException;
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
        try {
            this.id = UUID.randomUUID();
            this.sourceWalletId = Objects.requireNonNull(sourceWalletId, "Source wallet cannot be null");
            this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
            this.amount = validateAmount(amount);
            this.destinationWalletId = destinationWalletId;
            this.timestamp = LocalDateTime.now();
        }catch (NullPointerException e) {
            throw new NonNullValueException(e.getMessage());
        }
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
        if(destination == null) {
            throw new NonNullValueException("Source wallet cannot be null");
        }
        return new Transaction(
                source,
                destination,
                TransactionType.TRANSFER,
                amount);
    }

    public static Transaction createDeposit(UUID source, BigDecimal amount) {
        return new Transaction(source, null, TransactionType.DEPOSIT, amount);
    }

    public static Transaction createWithdraw(UUID source, BigDecimal amount
    ) {
        return new Transaction(source, null, TransactionType.WITHDRAW, amount);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountTransactionException("Amount must be more than 0");
        }
        if (amount.scale() > 2) {
            throw new InvalidAmountTransactionException("Amount cannot have more than 2 decimal places");
        }
        return amount;
    }

}
