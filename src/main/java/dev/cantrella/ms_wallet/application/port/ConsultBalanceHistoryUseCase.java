package dev.cantrella.ms_wallet.application.port;

import dev.cantrella.ms_wallet.application.dto.BalanceHistoryQuery;
import dev.cantrella.ms_wallet.application.dto.BalanceResponse;

public interface ConsultBalanceHistoryUseCase {

    BalanceResponse execute(BalanceHistoryQuery query);
}
