package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.BalanceHistoryQuery;
import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.application.port.ConsultBalanceHistoryUseCase;
import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.domain.model.TransactionType;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionLogRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultBalanceHistoryUseCaseImpl implements ConsultBalanceHistoryUseCase {

    private final TransactionLogRepositoryPort transactionLogRepositoryPort;
    private final WalletRepositoryPort walletRepositoryPort;
    private final CachePort cachePort;


    @Override
    public BalanceResponse execute(BalanceHistoryQuery query) {
        String key = buildCacheKey(query.userId(), query.timestamp());
        BalanceResponse cachedBalance = cachePort.get(key, BalanceResponse.class);
        if(cachedBalance != null) {
            return cachedBalance;
        }
        Wallet wallet = walletRepositoryPort.findByUserId(query.userId())
                .orElseThrow(() -> new WalletNotFoundException(query.userId()));
        List<Transaction> transactions = transactionLogRepositoryPort.listByWalletId(wallet.getId(), query.timestamp());
        BigDecimal balance = transactions.stream()
                .map(tx -> {
                    if (isCashIn(tx, wallet.getId())) {
                        return tx.getAmount();
                    } else {
                        return tx.getAmount().negate();
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BalanceResponse balanceResponse = new BalanceResponse(wallet.getId(), balance);
        cachePort.put(key, balanceResponse);
        return balanceResponse;
    }

    private boolean isCashIn(Transaction transaction, UUID walletId) {
        if(TransactionType.DEPOSIT.equals(transaction.getType())) {
            return true;
        }
        if(TransactionType.WITHDRAW.equals(transaction.getType())) {
            return false;
        }
        return !walletId.equals(transaction.getSourceWalletId());
    }

    private String buildCacheKey(String userId, LocalDateTime at) {
        return String.format("wallet:%s:balance:%s", userId, at.toString());
    }
}
