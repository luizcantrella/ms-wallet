package dev.cantrella.ms_wallet.application.exception;

public class WalletAlreadyExistsException extends ApplicationException {
    public WalletAlreadyExistsException() {
        super("Wallet already exists");
    }
}
