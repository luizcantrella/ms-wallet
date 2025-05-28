package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.TransferCommand;
import dev.cantrella.ms_wallet.domain.model.Transaction;

public interface TransferUseCase {
    Transaction execute(TransferCommand command);
}
