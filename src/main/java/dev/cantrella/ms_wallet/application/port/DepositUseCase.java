package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.DepositOrWithdrawCommand;
import dev.cantrella.ms_wallet.domain.model.Transaction;

public interface DepositUseCase {
    Transaction execute(DepositOrWithdrawCommand command);
}
