package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.TransferCommand;
import dev.cantrella.ms_wallet.application.exception.OperationNotAllowedException;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.domain.exception.InvalidAmountTransactionException;
import dev.cantrella.ms_wallet.domain.exception.WalletOperationException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferUseCaseImplTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private TransferUseCaseImpl transferUseCase;

    String sourceUserId = "bob@mail.com";
    String destinationUserId = "john@mail.com";
    UUID sourceWalletId = UUID.randomUUID();
    UUID destinationWalletId = UUID.randomUUID();

    @Test
    @DisplayName("Should transfer amount successfully between wallets")
    void shouldTransferAmountSuccessfully() {
        BigDecimal amount = new BigDecimal("100.50");
        TransferCommand command = new TransferCommand(sourceWalletId, destinationWalletId, amount);
        Wallet sourceWallet = new Wallet(sourceWalletId, sourceUserId, new BigDecimal("200.00"), LocalDateTime.now());
        Wallet destinationWallet = new Wallet(destinationWalletId, destinationUserId, new BigDecimal("50.00"), LocalDateTime.now());
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepositoryPort.findByIdForUpdate(destinationWalletId)).thenReturn(Optional.of(destinationWallet));
        Transaction expectedTransaction = Transaction.createTransfer(sourceWalletId, destinationWalletId, amount);
        doNothing().when(transactionRepositoryPort).save(any());

        Transaction result = transferUseCase.execute(command);

        assertNotNull(result);
        assertEquals(sourceWalletId, result.getSourceWalletId());
        assertEquals(destinationWalletId, result.getDestinationWalletId());
        assertEquals(amount, result.getAmount());
        assertEquals(new BigDecimal("99.50"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("150.50"), destinationWallet.getBalance());
        assertEquals(TransactionType.TRANSFER, result.getType());
        verify(walletRepositoryPort).update(sourceWallet);
        verify(walletRepositoryPort).update(destinationWallet);
        verify(cachePort).evict(sourceUserId);
        verify(cachePort).evict(destinationUserId);
        verify(transactionRepositoryPort).save(result);
    }

    @Test
    @DisplayName("Should throw exception when source wallet not found")
    void shouldThrowExceptionWhenSourceWalletNotFound() {
        BigDecimal amount = new BigDecimal("100.00");
        TransferCommand command = new TransferCommand(sourceWalletId, destinationWalletId, amount);
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.empty());

        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class,
                () -> transferUseCase.execute(command));

        assertEquals(sourceWalletId, exception.getWalletId());
        verify(walletRepositoryPort, never()).update(any());
        verify(cachePort, never()).evict(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when destination wallet not found")
    void shouldThrowExceptionWhenDestinationWalletNotFound() {
        UUID sourceWalletId = UUID.randomUUID();
        UUID destinationWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        TransferCommand command = new TransferCommand(sourceWalletId, destinationWalletId, amount);
        Wallet sourceWallet = new Wallet(sourceWalletId, sourceUserId, new BigDecimal("200.00"), LocalDateTime.now());
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepositoryPort.findByIdForUpdate(destinationWalletId)).thenReturn(Optional.empty());

        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class,
                () -> transferUseCase.execute(command));

        assertEquals(destinationWalletId, exception.getWalletId());
        verify(walletRepositoryPort, never()).update(any());
        verify(cachePort, never()).evict(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when source wallet has insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        BigDecimal amount = new BigDecimal("300.00");
        TransferCommand command = new TransferCommand(sourceWalletId, destinationWalletId, amount);
        Wallet sourceWallet = new Wallet(sourceWalletId, sourceUserId, new BigDecimal("200.00"), LocalDateTime.now());
        Wallet destinationWallet = new Wallet(destinationWalletId, destinationUserId, new BigDecimal("50.00"), LocalDateTime.now());
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepositoryPort.findByIdForUpdate(destinationWalletId)).thenReturn(Optional.of(destinationWallet));

        assertThrows(WalletOperationException.class, () -> transferUseCase.execute(command));

        verify(walletRepositoryPort, never()).update(any());
        verify(cachePort, never()).evict(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when source wallet and destination wallet equals")
    void shouldThrowExceptionWhenWalletsAreEquals() {
        BigDecimal amount = new BigDecimal("300.00");
        TransferCommand command = new TransferCommand(sourceWalletId, sourceWalletId, amount);
        Wallet sourceWallet = new Wallet(sourceWalletId, sourceUserId, new BigDecimal("200.00"), LocalDateTime.now());
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.of(sourceWallet));

        var exception = assertThrows(OperationNotAllowedException.class, () -> transferUseCase.execute(command));

        assertEquals("Source wallet and destination wallet is same", exception.getMessage());
        verify(walletRepositoryPort, never()).update(any());
        verify(cachePort, never()).evict(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void shouldThrowExceptionWhenAmountIsInvalid() {
        TransferCommand zeroAmountCommand = new TransferCommand(sourceWalletId, destinationWalletId, BigDecimal.ZERO);
        TransferCommand negativeAmountCommand = new TransferCommand(sourceWalletId, destinationWalletId, new BigDecimal("-10.00"));
        Wallet sourceWallet = new Wallet(sourceWalletId, sourceUserId, new BigDecimal("200.00"), LocalDateTime.now());
        Wallet destinationWallet = new Wallet(destinationWalletId, destinationUserId, new BigDecimal("50.00"), LocalDateTime.now());
        when(walletRepositoryPort.findByIdForUpdate(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepositoryPort.findByIdForUpdate(destinationWalletId)).thenReturn(Optional.of(destinationWallet));

        assertThrows(InvalidAmountTransactionException.class, () -> transferUseCase.execute(zeroAmountCommand));
        assertThrows(InvalidAmountTransactionException.class, () -> transferUseCase.execute(negativeAmountCommand));

        verify(walletRepositoryPort, times(4)).findByIdForUpdate(any());
        verify(walletRepositoryPort, never()).update(any());
        verify(cachePort, never()).evict(any());
        verify(transactionRepositoryPort, never()).save(any());
    }

}