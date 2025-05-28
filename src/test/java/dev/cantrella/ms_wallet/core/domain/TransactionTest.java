package dev.cantrella.ms_wallet.core.domain;

import dev.cantrella.ms_wallet.domain.exception.InvalidAmountTransactionException;
import dev.cantrella.ms_wallet.domain.exception.NonNullValueException;
import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private UUID sourceWalletId;
    private UUID destinationWalletId;
    private BigDecimal validAmount;

    @BeforeEach
    void setUp() {
        sourceWalletId = UUID.randomUUID();
        destinationWalletId = UUID.randomUUID();
        validAmount = new BigDecimal("100.50");
    }

    @Test
    @DisplayName("createTransfer should create valid transfer transaction")
    void createTransfer_shouldCreateValidTransaction() {
        Transaction transaction = Transaction.createTransfer(
                sourceWalletId,
                destinationWalletId,
                validAmount);

        assertNotNull(transaction.getId());
        assertEquals(sourceWalletId, transaction.getSourceWalletId());
        assertEquals(destinationWalletId, transaction.getDestinationWalletId());
        assertEquals(TransactionType.TRANSFER, transaction.getType());
        assertEquals(validAmount, transaction.getAmount());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    @DisplayName("createDeposit should create valid deposit transaction")
    void createDeposit_shouldCreateValidTransaction() {
        Transaction transaction = Transaction.createDeposit(
                sourceWalletId,
                validAmount);

        assertNotNull(transaction.getId());
        assertEquals(sourceWalletId, transaction.getSourceWalletId());
        assertNull(transaction.getDestinationWalletId());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(validAmount, transaction.getAmount());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    @DisplayName("createWithdraw should create valid withdraw transaction")
    void createWithdraw_shouldCreateValidTransaction() {
        Transaction transaction = Transaction.createWithdraw(
                sourceWalletId,
                validAmount);

        assertNotNull(transaction.getId());
        assertEquals(sourceWalletId, transaction.getSourceWalletId());
        assertNull(transaction.getDestinationWalletId());
        assertEquals(TransactionType.WITHDRAW, transaction.getType());
        assertEquals(validAmount, transaction.getAmount());
        assertNotNull(transaction.getTimestamp());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("createTransfer should throw when source or destination is null")
    void createTransfer_shouldThrowWhenParamsAreNull(UUID nullId) {
        assertThrows(NonNullValueException.class, () ->
                        Transaction.createTransfer(null, destinationWalletId, validAmount),
                "Source wallet cannot be null");
        assertThrows(NonNullValueException.class, () ->
                Transaction.createTransfer(sourceWalletId, null, validAmount));
    }

    @Test
    @DisplayName("createDeposit should throw when source is null")
    void createDeposit_shouldThrowWhenSourceIsNull() {
        assertThrows(NonNullValueException.class, () ->
                        Transaction.createDeposit(null, validAmount),
                "Source wallet cannot be null");
    }

    @Test
    @DisplayName("createWithdraw should throw when source is null")
    void createWithdraw_shouldThrowWhenSourceIsNull() {
        assertThrows(NonNullValueException.class, () ->
                        Transaction.createWithdraw(null, validAmount),
                "Source wallet cannot be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    @DisplayName("should reject zero or negative amounts")
    void shouldRejectZeroOrNegativeAmounts(String invalidAmount) {
        BigDecimal amount = new BigDecimal(invalidAmount);

        assertThrows(InvalidAmountTransactionException.class, () ->
                        Transaction.createDeposit(sourceWalletId, amount),
                "Amount must be positive");
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.123", "50.555", "0.001"})
    @DisplayName("should reject amounts with more than 2 decimal places")
    void shouldRejectAmountsWithManyDecimals(String invalidAmount) {
        BigDecimal amount = new BigDecimal(invalidAmount);

        assertThrows(InvalidAmountTransactionException.class, () ->
                        Transaction.createDeposit(sourceWalletId, amount),
                "Amount cannot have more than 2 decimal places");
    }

    @Test
    @DisplayName("should generate unique IDs for each transaction")
    void shouldGenerateUniqueIds() {
        Transaction t1 = Transaction.createDeposit(sourceWalletId, validAmount);
        Transaction t2 = Transaction.createDeposit(sourceWalletId, validAmount);

        assertNotEquals(t1.getId(), t2.getId());
    }

    @Test
    @DisplayName("should set current timestamp on creation")
    void shouldSetCurrentTimestamp() {
        LocalDateTime before = LocalDateTime.now();
        Transaction transaction = Transaction.createDeposit(sourceWalletId, validAmount);
        LocalDateTime after = LocalDateTime.now();

        assertTrue(transaction.getTimestamp().isAfter(before) ||
                transaction.getTimestamp().equals(before));
        assertTrue(transaction.getTimestamp().isBefore(after) ||
                transaction.getTimestamp().equals(after));
    }

    @Test
    @DisplayName("transfer should have destination wallet")
    void transferShouldHaveDestination() {
        Transaction transfer = Transaction.createTransfer(
                sourceWalletId,
                destinationWalletId,
                validAmount);

        assertNotNull(transfer.getDestinationWalletId());
    }

    @Test
    @DisplayName("deposit should not have destination wallet")
    void depositShouldNotHaveDestination() {
        Transaction deposit = Transaction.createDeposit(
                sourceWalletId,
                validAmount);

        assertNull(deposit.getDestinationWalletId());
    }

    @Test
    @DisplayName("withdraw should not have destination wallet")
    void withdrawShouldNotHaveDestination() {
        Transaction withdraw = Transaction.createWithdraw(
                sourceWalletId,
                validAmount);

        assertNull(withdraw.getDestinationWalletId());
    }

    private static Stream<Arguments> validAmountsProvider() {
        return Stream.of(
                Arguments.of("0.01"),
                Arguments.of("1.00"),
                Arguments.of("100.00"),
                Arguments.of("999999.99"),
                Arguments.of("0.99")
        );
    }

    @ParameterizedTest
    @MethodSource("validAmountsProvider")
    @DisplayName("should accept valid amounts with up to 2 decimal places")
    void shouldAcceptValidAmounts(String validAmount) {
        BigDecimal amount = new BigDecimal(validAmount);
        Transaction transaction = Transaction.createDeposit(
                sourceWalletId,
                amount);

        assertEquals(amount, transaction.getAmount());
    }
}