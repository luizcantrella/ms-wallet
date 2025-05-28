package dev.cantrella.ms_wallet.application.dto;

import dev.cantrella.ms_wallet.application.exception.InvalidDataException;

import java.time.LocalDateTime;
import java.util.Objects;

public record BalanceHistoryQuery(String userId, LocalDateTime timestamp) {
    public BalanceHistoryQuery {
        try {
            Objects.requireNonNull(userId, "UserId can not be null");
            Objects.requireNonNull(timestamp, "Amount can not be null");
        } catch (NullPointerException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
}
