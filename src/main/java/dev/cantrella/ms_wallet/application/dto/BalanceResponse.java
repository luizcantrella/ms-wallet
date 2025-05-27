package dev.cantrella.ms_wallet.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID walletId, BigDecimal balance) {
}
