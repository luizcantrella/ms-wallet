package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.BalanceHistoryQuery;
import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.domain.Transaction;
import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.TransactionLogRepositoryPort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultBalanceHistoryUseCaseTest {

    @Mock
    private TransactionLogRepositoryPort transactionLogRepositoryPort;
    @Mock
    private WalletRepositoryPort walletRepositoryPort;
    @Mock
    private CachePort cachePort;
    @InjectMocks
    private ConsultBalanceHistoryUseCaseImpl consultBalanceHistoryUseCase;
    private Wallet wallet;

    private final static String USER_ID = "bob@mail.com";

    @BeforeEach
    void setUp() {
        wallet = Wallet.create("user_id");
    }

    @Test
    void shouldReturnBalanceWithDepositAndWithdraw() {
        // Arrange
        BalanceHistoryQuery query = new BalanceHistoryQuery(
                USER_ID, LocalDateTime.now());

        when(walletRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));

        List<Transaction> transactions = Arrays.asList(
                Transaction.createDeposit(wallet.getId(), new BigDecimal("100.00"), "BRL"),
                Transaction.createWithdraw(wallet.getId(), new BigDecimal("50.00"), "BRL")
        );

        when(transactionLogRepositoryPort.listByWalletId(eq(wallet.getId()), any()))
                .thenReturn(transactions);

        // Act
        BalanceResponse response = consultBalanceHistoryUseCase.execute(query);

        // Assert
        assertNotNull(response);
        assertEquals(wallet.getId(), response.walletId());
        assertEquals(new BigDecimal("50.00"), response.balance());
    }

    @Test
    void shouldReturnBalanceWithTransferAsCashOut() {
        // Arrange
        BalanceHistoryQuery query = new BalanceHistoryQuery(USER_ID, LocalDateTime.now());

        when(walletRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));

        List<Transaction> transactions = Arrays.asList(
                Transaction.createDeposit(wallet.getId(), new BigDecimal("100.00"), "BRL"),
                Transaction.createTransfer(
                        wallet.getId(),
                        UUID.randomUUID(),
                        new BigDecimal("30.00"),
                        "BRL")
        );

        when(transactionLogRepositoryPort.listByWalletId(eq(wallet.getId()), any()))
                .thenReturn(transactions);

        // Act
        BalanceResponse response = consultBalanceHistoryUseCase.execute(query);

        // Assert
        assertEquals(new BigDecimal("70.00"), response.balance());
    }

    @Test
    void shouldReturnBalanceWithTransferAsCashIn() {

        BalanceHistoryQuery query = new BalanceHistoryQuery(USER_ID, LocalDateTime.now());
        when(walletRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
        List<Transaction> transactions = Arrays.asList(
                Transaction.createTransfer(UUID.randomUUID(), wallet.getId(), new BigDecimal("40.00"), "BRL")
        );
        when(transactionLogRepositoryPort.listByWalletId(eq(wallet.getId()), any()))
                .thenReturn(transactions);

        BalanceResponse response = consultBalanceHistoryUseCase.execute(query);

        assertEquals(new BigDecimal("40.00"), response.balance());
    }

    @Test
    void shouldThrowExceptionWhenWalletDoesNotExist() {

        BalanceHistoryQuery query = new BalanceHistoryQuery(USER_ID, LocalDateTime.now());
        when(walletRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consultBalanceHistoryUseCase.execute(query));
        assertEquals("Wallet don't exists", exception.getMessage());
    }

    @Test
    void shouldReturnZeroWhenNoTransactions() {

        BalanceHistoryQuery query = new BalanceHistoryQuery(USER_ID, LocalDateTime.now());
        when(walletRepositoryPort.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
        when(transactionLogRepositoryPort.listByWalletId(eq(wallet.getId()), any()))
                .thenReturn(List.of());

        BalanceResponse response = consultBalanceHistoryUseCase.execute(query);

        assertEquals(BigDecimal.ZERO, response.balance());
    }
}
