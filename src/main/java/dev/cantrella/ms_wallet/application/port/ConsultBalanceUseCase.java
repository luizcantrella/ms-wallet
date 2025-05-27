package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.application.dto.ConsultBalanceQuery;

public interface ConsultBalanceUseCase {

    BalanceResponse execute(ConsultBalanceQuery command);
}
