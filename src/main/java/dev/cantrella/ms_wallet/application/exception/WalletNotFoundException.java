package dev.cantrella.ms_wallet.application.exception;

import java.util.UUID;

public class WalletNotFoundException extends ApplicationException {
    private final UUID walletId;
    private final String userId;
    public WalletNotFoundException(UUID walletId) {
        super("Wallet with ID " + walletId + " not found");
        this.walletId = walletId;
        this.userId = null;
    }

    public WalletNotFoundException(String userid) {
        super("Wallet for user ID " + userid + " not found");
        this.userId = userid;
        this.walletId = null;
    }

    public UUID getWalletId() { return this.walletId; }
    public String getUserId() { return this.userId; }
}
