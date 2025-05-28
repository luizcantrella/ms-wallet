package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.DepositOrWithdrawCommand;
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
                LocalDateTime.now());
    }

    @Test
    @DisplayName("Should deposit amount successfully when wallet exists")
    void shouldDepositAmountSuccessfully() {
        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("100.50");
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);
        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));
        when(transactionRepositoryPort.save(any())).thenReturn(transaction);
        Transaction expectedTransaction = Transaction.createDeposit(wallet.getId(), amount);

        Transaction result = depositUseCase.execute(command);

        assertNotNull(result);
        assertEquals(amount, wallet.getBalance());
        verify(walletRepositoryPort).findByUserIdForUpdate(userEmail);
        verify(walletRepositoryPort).update(wallet);
        verify(transactionRepositoryPort).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("50.00");
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);

        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> depositUseCase.execute(command));

        assertEquals("WalletNotFound", exception.getMessage());
        verify(walletRepositoryPort).findByUserIdForUpdate(userEmail);
        verify(walletRepositoryPort, never()).update(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {

        String userEmail = "bob@mail.com";

        assertThrows(NullPointerException.class,
                () -> depositUseCase.execute(new DepositOrWithdrawCommand(userEmail, null)));

        verify(walletRepositoryPort, never()).findByUserIdForUpdate(any());
        verify(walletRepositoryPort, never()).update(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void shouldThrowExceptionWhenAmountIsInvalid() {

        String userEmail = "bob@mail.com";
        DepositOrWithdrawCommand zeroAmountCommand = new DepositOrWithdrawCommand(userEmail, BigDecimal.ZERO);
        DepositOrWithdrawCommand negativeAmountCommand = new DepositOrWithdrawCommand(userEmail, new BigDecimal("-10.00"));

        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> depositUseCase.execute(zeroAmountCommand));
        assertThrows(RuntimeException.class, () -> depositUseCase.execute(negativeAmountCommand));
    }

    @Test
    @DisplayName("Should create transaction with correct values")
    void shouldCreateTransactionWithCorrectValues() {

        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("75.25");
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);
        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, BigDecimal.ZERO, LocalDateTime.now());
        Transaction depositTransaction = Transaction.createDeposit(wallet.getId(), amount);
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));
        when(transactionRepositoryPort.save(any())).thenReturn(depositTransaction);

        Transaction result = depositUseCase.execute(command);


        assertNotNull(result);
        assertEquals(wallet.getId(), result.getSourceWalletId());
        assertEquals(amount, result.getAmount());
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertNull(result.getDestinationWalletId());
    }

    @Test
    @DisplayName("Should update wallet balance correctly")
    void shouldUpdateWalletBalanceCorrectly() {

        String userEmail = "bob@mail.com";
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal depositAmount = new BigDecimal("30.50");
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, depositAmount);
        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, initialBalance, LocalDateTime.now());
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));

        depositUseCase.execute(command);

        assertEquals(initialBalance.add(depositAmount), wallet.getBalance());
    }

    @Test
    @DisplayName("Should return the created transaction")
    void shouldReturnCreatedTransaction() {

        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("100.00");

        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);

        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, BigDecimal.ZERO, LocalDateTime.now());
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));

        Transaction expectedTransaction = Transaction.createDeposit(wallet.getId(), amount);
        when(transactionRepositoryPort.save(any())).thenReturn(expectedTransaction);

        Transaction result = depositUseCase.execute(command);

        assertSame(expectedTransaction, result);
    }
}