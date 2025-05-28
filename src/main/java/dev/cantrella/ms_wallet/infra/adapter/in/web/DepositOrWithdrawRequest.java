package dev.cantrella.ms_wallet.infra.adapter.in.web;


import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositOrWithdrawRequest(@NotNull BigDecimal amount) {
}
