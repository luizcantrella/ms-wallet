package dev.cantrella.ms_wallet.application.dto;

import java.util.Objects;

public record CreateWalletCommand(String userId) {
    public CreateWalletCommand {
        Objects.requireNonNull(userId, "UserId can not be null");
    }
}
