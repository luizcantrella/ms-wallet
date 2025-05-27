package dev.cantrella.ms_wallet.infra.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotBlank(message = "ID da carteira de origem é obrigatório")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "ID da carteira de origem deve ser um UUID válido")
        String sourceWalletId,

        @NotBlank(message = "ID da carteira de destino é obrigatório")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "ID da carteira de destino deve ser um UUID válido")
        String destinationWalletId,

        @NotNull(message = "Valor é obrigatório")
        BigDecimal amount) {
}
