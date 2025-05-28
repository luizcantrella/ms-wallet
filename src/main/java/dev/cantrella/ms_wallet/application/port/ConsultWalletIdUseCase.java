package dev.cantrella.ms_wallet.application.port;

import java.util.UUID;

public interface ConsultWalletIdUseCase {

    UUID execute(String userId);
}
