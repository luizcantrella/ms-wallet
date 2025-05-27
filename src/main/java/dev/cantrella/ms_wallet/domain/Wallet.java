package dev.cantrella.ms_wallet.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class Wallet {

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
        return new Wallet(
                UUID.randomUUID(),
                userId,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("Negative value is not allowed in this operations");
        }
        this.balance = this.balance.add(amount);
    }
    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            // not allowed operation, amount invalid
            throw new RuntimeException("Negative value is not allowed in this operations");
        }
        BigDecimal result = this.balance.subtract(amount);
        if (result.signum() < 0) {
            // not allowed operation, wallet balance isn't enough
            throw new RuntimeException("The wallet balance don't have enough amount to withdraw");
        }
        this.balance = result;
    }

}
