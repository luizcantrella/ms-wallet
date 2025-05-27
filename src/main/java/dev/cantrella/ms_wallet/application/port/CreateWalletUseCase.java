package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.CreateWalletCommand;

import java.util.UUID;

public interface CreateWalletUseCase {
    UUID execute(CreateWalletCommand command);
}
