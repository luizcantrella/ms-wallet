package dev.cantrella.ms_wallet.application.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record BalanceHistoryQuery(UUID walletId, LocalDateTime timestamp) {
    public BalanceHistoryQuery {
        Objects.requireNonNull(walletId, "WalletId can not be null");
        Objects.requireNonNull(timestamp, "Amount can not be null");
    }
}
