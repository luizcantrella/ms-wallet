package dev.cantrella.ms_wallet.application.port;

import java.util.UUID;

public interface CreateWalletUseCase {
    UUID execute(String userEmail);
}
