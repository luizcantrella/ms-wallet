package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.application.dto.ConsultBalanceQuery;
import dev.cantrella.ms_wallet.application.port.ConsultBalanceUseCase;
import dev.cantrella.ms_wallet.domain.Wallet;
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
    public BalanceResponse execute(ConsultBalanceQuery command) {
        BalanceResponse cachedBalance = cachePort.get(command.userId(), BalanceResponse.class);
        if(cachedBalance != null) {
            return cachedBalance;
        }
        Wallet wallet = walletRepositoryPort
                .findByUserId(command.userId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        var balanceResponse = new BalanceResponse(wallet.getId(), wallet.getBalance());
        cachePort.put(command.userId(), balanceResponse);
        return balanceResponse;
    }
}
