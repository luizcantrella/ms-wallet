package dev.cantrella.ms_wallet.application.dto;

import java.util.Objects;
import java.util.UUID;

public record ConsultBalanceQuery(String userId) {

    public ConsultBalanceQuery {
        Objects.requireNonNull(userId, "UserId can not be null");
    }
}
