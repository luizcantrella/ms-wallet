package dev.cantrella.ms_wallet.application.dto;

import dev.cantrella.ms_wallet.application.exception.InvalidDataException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record BalanceResponse(UUID walletId, BigDecimal balance) {
    public BalanceResponse{
        try {
            Objects.requireNonNull(walletId, "Wallet id can not be null");
            Objects.requireNonNull(balance, "Balance can not be null");
        } catch (NullPointerException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
}
