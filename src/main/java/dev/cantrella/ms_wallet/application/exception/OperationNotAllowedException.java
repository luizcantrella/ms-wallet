package dev.cantrella.ms_wallet.application.exception;

public class OperationNotAllowedException extends ApplicationException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
}
