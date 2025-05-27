package dev.cantrella.ms_wallet.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record TransferCommand(UUID sourceWalletId, UUID destinationWalletId, BigDecimal amount) {

    public TransferCommand {
        Objects.requireNonNull(sourceWalletId, "WalletId can not be null");
        Objects.requireNonNull(destinationWalletId, "WalletId can not be null");
        Objects.requireNonNull(amount, "Amount can not be null");
    }
}
