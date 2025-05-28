package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.DepositOrWithdrawCommand;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.domain.exception.InvalidAmountTransactionException;
import dev.cantrella.ms_wallet.domain.model.Transaction;
import dev.cantrella.ms_wallet.domain.model.TransactionType;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WithdrawUseCaseImplTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private CachePort cachePort;


    @InjectMocks
    private WithdrawUseCaseImpl withdrawUseCase;

    @Test
    @DisplayName("Should withdraw amount successfully when wallet exists")
    void shouldWithdrawAmountSuccessfully() {
        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("100.50");
        BigDecimal balance = new BigDecimal("200.00");
        BigDecimal balanceExpected = balance.subtract(amount);
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);
        Wallet wallet = new Wallet(UUID.randomUUID(), userEmail, balance, LocalDateTime.now());
        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.of(wallet));
        doNothing().when(transactionRepositoryPort).save(any());
        LocalDateTime before = LocalDateTime.now();

        Transaction result = withdrawUseCase.execute(command);

        LocalDateTime after = LocalDateTime.now();
        assertNotNull(result);
        assertEquals(balanceExpected, wallet.getBalance());
        assertEquals(amount, result.getAmount());
        assertEquals(wallet.getId(), result.getSourceWalletId());
        assertEquals(TransactionType.WITHDRAW, result.getType());
        assertTrue(before.isBefore(result.getTimestamp()) || before.isEqual(result.getTimestamp()));
        assertTrue(after.isAfter(result.getTimestamp()) || after.isEqual(result.getTimestamp()));
        assertNull(result.getDestinationWalletId());
        verify(walletRepositoryPort).findByUserIdForUpdate(userEmail);
        verify(walletRepositoryPort).update(wallet);
        verify(transactionRepositoryPort).save(result);
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        String userEmail = "bob@mail.com";
        BigDecimal amount = new BigDecimal("50.00");
        DepositOrWithdrawCommand command = new DepositOrWithdrawCommand(userEmail, amount);

        when(walletRepositoryPort.findByUserIdForUpdate(userEmail)).thenReturn(Optional.empty());

        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class,
                () -> withdrawUseCase.execute(command));

        assertEquals("Wallet for user ID " + userEmail + " not found", exception.getMessage());
        verify(walletRepositoryPort).findByUserIdForUpdate(userEmail);
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

        assertThrows(InvalidAmountTransactionException.class, () -> withdrawUseCase.execute(zeroAmountCommand));
        assertThrows(InvalidAmountTransactionException.class, () -> withdrawUseCase.execute(negativeAmountCommand));
    }
}