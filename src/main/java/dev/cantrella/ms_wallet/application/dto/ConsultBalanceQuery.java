package dev.cantrella.ms_wallet.application.dto;

import java.util.Objects;
import java.util.UUID;

public record ConsultBalanceQuery(UUID walletId) {

    public ConsultBalanceQuery {
        Objects.requireNonNull(walletId, "UserId can not be null");
    }
}
