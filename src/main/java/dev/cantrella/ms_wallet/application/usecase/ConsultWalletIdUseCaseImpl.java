package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.application.port.ConsultWalletIdUseCase;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultWalletIdUseCaseImpl implements ConsultWalletIdUseCase {

    private final WalletRepositoryPort walletRepositoryPort;
    private final CachePort cachePort;
    @Override
    public UUID execute(String userId) {
        String cacheKey = "wallet:id:".concat(userId);
        String walletId = cachePort.get(cacheKey, String.class);
        if(walletId != null) {
            return UUID.fromString(walletId);
        }
        UUID id = walletRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId)).getId();
        cachePort.put(cacheKey, id.toString());
        return id;
    }
}
