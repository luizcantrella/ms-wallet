package dev.cantrella.ms_wallet.core.domain;

import dev.cantrella.ms_wallet.domain.exception.NonNullValueException;
import dev.cantrella.ms_wallet.domain.exception.WalletOperationException;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    private Wallet wallet;
    private final String testUserId = UUID.randomUUID().toString();
    private final UUID testWalletId = UUID.randomUUID();
    private final LocalDateTime testDateTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        wallet = new Wallet(testWalletId, testUserId, BigDecimal.valueOf(100), testDateTime);
    }

    @Test
    @DisplayName("createNewWallet should create wallet with zero balance and new UUID")
    void createNewWallet_shouldCreateWalletWithCorrectInitialValues() {
        Wallet newWallet = Wallet.create(testUserId);

        assertNotNull(newWallet.getId());
        assertEquals(testUserId, newWallet.getUserId());
        assertEquals(BigDecimal.ZERO, newWallet.getBalance());
        assertNotNull(newWallet.getCreatedAt());
        assertNotEquals(testWalletId, newWallet.getId());
    }

    @Test
    @DisplayName("deposit should increase balance with positive amount")
    void deposit_shouldIncreaseBalance_whenPositiveAmount() {
        BigDecimal initialBalance = wallet.getBalance();
        BigDecimal depositAmount = BigDecimal.valueOf(50.75);

        wallet.deposit(depositAmount);

        assertEquals(initialBalance.add(depositAmount), wallet.getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    @DisplayName("deposit should throw exception when amount is zero or negative")
    void deposit_shouldThrowException_whenAmountZeroOrNegative(String amount) {
        BigDecimal invalidAmount = new BigDecimal(amount);

        WalletOperationException exception = assertThrows(WalletOperationException.class,
                () -> wallet.deposit(invalidAmount));

        assertEquals("Negative value is not allowed in this operations", exception.getMessage());
    }

    @Test
    @DisplayName("deposit should throw exception when amount is null")
    void create_shouldThrowException_whenUserIdIsNull() {
        assertThrows(NonNullValueException.class,
                () -> Wallet.create(null),
                "UserId is required to create a wallet");
    }
    @Test
    @DisplayName("deposit should throw exception when amount is null")
    void deposit_shouldThrowException_whenAmountIsNull() {
        assertThrows(WalletOperationException.class,
                () -> wallet.deposit(null),
                "Negative value is not allowed in this operations");
    }

    @Test
    @DisplayName("withdraw should decrease balance with positive amount")
    void withdraw_shouldDecreaseBalance_whenPositiveAmount() {
        BigDecimal initialBalance = wallet.getBalance();
        BigDecimal withdrawAmount = BigDecimal.valueOf(30.25);

        wallet.withdraw(withdrawAmount);

        assertEquals(initialBalance.subtract(withdrawAmount), wallet.getBalance());
    }

    @Test
    @DisplayName("withdraw should allow balance to go to zero")
    void withdraw_shouldAllowZeroBalance() {
        wallet = new Wallet(testWalletId, testUserId, BigDecimal.valueOf(50), testDateTime);
        wallet.withdraw(BigDecimal.valueOf(50));

        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    @DisplayName("withdraw should throw exception when amount is zero or negative")
    void withdraw_shouldThrowException_whenAmountZeroOrNegative(String amount) {
        BigDecimal invalidAmount = new BigDecimal(amount);

        WalletOperationException exception = assertThrows(WalletOperationException.class,
                () -> wallet.withdraw(invalidAmount));

        assertEquals("Negative value is not allowed in this operations", exception.getMessage());
    }

    @Test
    @DisplayName("withdraw should throw exception when amount is null")
    void withdraw_shouldThrowException_whenAmountIsNull() {
        assertThrows(WalletOperationException.class,
                () -> wallet.withdraw(null),
                "Negative value is not allowed in this operations");
    }

    @Test
    @DisplayName("withdraw should throw exception when wallet don't have enough balance")
    void withdraw_shouldThrowException_whenBalanceIsNotEnough() {
        assertThrows(WalletOperationException.class,
                () -> wallet.withdraw(new BigDecimal("200")),
                "The wallet balance don't have enough amount to withdraw");
    }

}
