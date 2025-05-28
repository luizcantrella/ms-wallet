package dev.cantrella.ms_wallet.application.dto;

import dev.cantrella.ms_wallet.application.exception.InvalidDataException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record TransferCommand(UUID sourceWalletId, UUID destinationWalletId, BigDecimal amount) {

    public TransferCommand {
        try {
            Objects.requireNonNull(sourceWalletId, "Source wallet id can not be null");
            Objects.requireNonNull(destinationWalletId, "Destination wallet id can not be null");
            Objects.requireNonNull(amount, "Amount can not be null");
        } catch (NullPointerException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }
}
