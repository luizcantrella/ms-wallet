package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.application.dto.ConsultBalanceQuery;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.application.port.ConsultBalanceUseCase;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultBalanceUseCaseImpl implements ConsultBalanceUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final CachePort cachePort;

    @Override
    public BalanceResponse execute(ConsultBalanceQuery query) {
        BalanceResponse cachedBalance = cachePort.get(query.userId(), BalanceResponse.class);
        if(cachedBalance != null) {
            return cachedBalance;
        }
        Wallet wallet = walletRepositoryPort
                .findByUserId(query.userId())
                .orElseThrow(() -> new WalletNotFoundException(query.userId()));
        var balanceResponse = new BalanceResponse(wallet.getId(), wallet.getBalance());
        cachePort.put(query.userId(), balanceResponse);
        return balanceResponse;
    }
}
