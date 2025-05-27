package dev.cantrella.ms_wallet.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record DepositCommand(UUID walletId, BigDecimal amount) {

    public DepositCommand {
        Objects.requireNonNull(walletId, "WalletId can not be null");
        Objects.requireNonNull(amount, "Amount can not be null");
    }
}
