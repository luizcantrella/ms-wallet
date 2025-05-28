package dev.cantrella.ms_wallet.application.dto;

import dev.cantrella.ms_wallet.application.exception.InvalidDataException;


public record ConsultBalanceQuery(String userId) {

    public ConsultBalanceQuery {
        if(userId == null || userId.isBlank()) {
            throw new InvalidDataException("user id can't be null");
        }
    }
}
