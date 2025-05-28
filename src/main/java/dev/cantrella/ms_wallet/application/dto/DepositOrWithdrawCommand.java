package dev.cantrella.ms_wallet.application.dto;

import dev.cantrella.ms_wallet.application.exception.InvalidDataException;

import java.math.BigDecimal;
import java.util.Objects;

public record DepositOrWithdrawCommand(String userId, BigDecimal amount) {

    public DepositOrWithdrawCommand {
        try {
            Objects.requireNonNull(userId, "WalletId can not be null");
            Objects.requireNonNull(amount, "Amount can not be null");
        } catch (NullPointerException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
}
