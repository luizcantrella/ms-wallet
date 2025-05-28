package dev.cantrella.ms_wallet.domain.exception;

public class WalletOperationException extends DomainException {
    public WalletOperationException(String message) {
        super(message);
    }
}
