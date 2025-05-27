package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.DepositCommand;
import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.domain.TransactionType;
import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositUseCaseImplTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private CachePort cachePort;


    @InjectMocks
    private DepositUseCaseImpl depositUseCase;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.reconstruct(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("20.00"),
                "BRL",
                LocalDateTime.now());
    }

    @Test
    @DisplayName("Should deposit amount successfully when wallet exists")
    void shouldDepositAmountSuccessfully() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.50");
        DepositCommand command = new DepositCommand(walletId, amount);

        Wallet wallet = new Wallet(walletId, "user_123", BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepositoryPort.save(any())).thenReturn(transaction);
        Transaction expectedTransaction = Transaction.createDeposit(walletId, amount, "BLR");

        // Act
        Transaction result = depositUseCase.execute(command);

        // Assert
        assertNotNull(result);
        assertEquals(amount, wallet.getBalance());
        verify(walletRepositoryPort).findById(walletId);
        verify(walletRepositoryPort).save(wallet);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50.00");
        DepositCommand command = new DepositCommand(walletId, amount);

        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> depositUseCase.execute(command));

        assertEquals("WalletNotFound", exception.getMessage());
        verify(walletRepositoryPort).findById(walletId);
        verify(walletRepositoryPort, never()).save(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        // Arrange
        UUID walletId = UUID.randomUUID();

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> depositUseCase.execute(new DepositCommand(walletId, null)));

        verify(walletRepositoryPort, never()).findById(any());
        verify(walletRepositoryPort, never()).save(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void shouldThrowExceptionWhenAmountIsInvalid() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        DepositCommand zeroAmountCommand = new DepositCommand(walletId, BigDecimal.ZERO);
        DepositCommand negativeAmountCommand = new DepositCommand(walletId, new BigDecimal("-10.00"));

        Wallet wallet = new Wallet(walletId, "user_123", BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> depositUseCase.execute(zeroAmountCommand));
        assertThrows(RuntimeException.class, () -> depositUseCase.execute(negativeAmountCommand));
    }

    @Test
    @DisplayName("Should create transaction with correct values")
    void shouldCreateTransactionWithCorrectValues() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("75.25");
        DepositCommand command = new DepositCommand(walletId, amount);

        Wallet wallet = new Wallet(walletId, "user_123", BigDecimal.ZERO, LocalDateTime.now());
        Transaction depositTransaction = Transaction.createDeposit(walletId, amount, "BRL");
        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.of(wallet));
        when(transactionRepositoryPort.save(any())).thenReturn(depositTransaction);
        // Act
        Transaction result = depositUseCase.execute(command);

        // Assert
        assertNotNull(result);
        assertEquals(walletId, result.getSourceWalletId());
        assertEquals(amount, result.getAmount());
        assertEquals("BRL", result.getCurrency());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertNull(result.getDestinationWalletId());
    }

    @Test
    @DisplayName("Should update wallet balance correctly")
    void shouldUpdateWalletBalanceCorrectly() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal depositAmount = new BigDecimal("30.50");
        DepositCommand command = new DepositCommand(walletId, depositAmount);

        Wallet wallet = new Wallet(walletId, "user_123", initialBalance, LocalDateTime.now());
        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act
        depositUseCase.execute(command);

        // Assert
        assertEquals(initialBalance.add(depositAmount), wallet.getBalance());
    }

    @Test
    @DisplayName("Should return the created transaction")
    void shouldReturnCreatedTransaction() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        DepositCommand command = new DepositCommand(walletId, amount);

        Wallet wallet = new Wallet(walletId, "user_123", BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction expectedTransaction = Transaction.createDeposit(walletId, amount, "BLR");
        when(transactionRepositoryPort.save(any())).thenReturn(expectedTransaction);

        // Act
        Transaction result = depositUseCase.execute(command);

        // Assert
        assertSame(expectedTransaction, result);
    }
}