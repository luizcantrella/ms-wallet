package dev.cantrella.ms_wallet.domain.exception;

public class InvalidAmountTransactionException extends DomainException{
    public InvalidAmountTransactionException(String message) {
        super(message);
    }
}
