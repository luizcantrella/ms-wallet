package dev.cantrella.ms_wallet.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record DepositOrWithdrawCommand(String userId, BigDecimal amount) {

    public DepositOrWithdrawCommand {
        Objects.requireNonNull(userId, "WalletId can not be null");
        Objects.requireNonNull(amount, "Amount can not be null");
    }
}
