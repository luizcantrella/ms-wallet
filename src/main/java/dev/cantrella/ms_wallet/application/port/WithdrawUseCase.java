package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.DepositCommand;
import dev.cantrella.ms_wallet.domain.Transaction;

public interface WithdrawUseCase {
    Transaction execute(DepositCommand command);
}
