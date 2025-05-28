package dev.cantrella.ms_wallet.domain.exception;

public class NonNullValueException extends DomainException{
    public NonNullValueException(String message) {
        super(message);
    }
}
