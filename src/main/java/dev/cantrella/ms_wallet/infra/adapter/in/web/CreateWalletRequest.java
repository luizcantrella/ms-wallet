package dev.cantrella.ms_wallet.infra.adapter.in.web;

import dev.cantrella.ms_wallet.application.dto.CreateWalletCommand;

import java.util.UUID;

public record CreateWalletRequest(String userId) {
}
