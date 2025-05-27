package dev.cantrella.ms_wallet.infra.adapter.in.web;


import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(@NotNull BigDecimal amount) {
}
