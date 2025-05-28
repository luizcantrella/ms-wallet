package dev.cantrella.ms_wallet.domain.model;

import dev.cantrella.ms_wallet.domain.exception.NonNullValueException;
import dev.cantrella.ms_wallet.domain.exception.WalletOperationException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wallet {
    @EqualsAndHashCode.Include
    private UUID id;
    private String userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public Wallet(UUID id, String userId, BigDecimal balance, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public static Wallet create(String userId) {
        if (userId == null) {
         throw new NonNullValueException("UserId is required to create a wallet");
        }
        return new Wallet(
                UUID.randomUUID(),
                userId,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new WalletOperationException("Negative value is not allowed in this operations");
        }
        this.balance = this.balance.add(amount);
    }
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new WalletOperationException("Negative value is not allowed in this operations");
        }
        BigDecimal result = this.balance.subtract(amount);
        if (result.signum() < 0) {
            throw new WalletOperationException("The wallet balance don't have enough amount to withdraw");
        }
        this.balance = result;
    }

}
