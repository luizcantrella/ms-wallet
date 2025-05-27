package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.CreateWalletCommand;
import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @InjectMocks
    private CreateWalletUseCaseImpl createWalletUseCase;

    @Mock
    private CachePort cachePort;

    private String userId;

    @BeforeEach
    void startUp() {
        userId = UUID.randomUUID().toString();
    }
    @Test
    @DisplayName("should create new wallet when user has no wallet")
    void shouldCreateNewWalletWhenUserHasNoWallet() {
        // Arrange
        CreateWalletCommand command = new CreateWalletCommand(userId);

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(walletRepositoryPort.save(any())).thenReturn(Wallet.create(userId));
        // Act
        createWalletUseCase.execute(command);

        // Assert
        verify(walletRepositoryPort, times(1)).existsByUserId(userId);
        verify(walletRepositoryPort, times(1)).save(argThat(wallet ->
                wallet.getUserId().equals(userId) &&
                        wallet.getBalance().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    @DisplayName("should throw exception when user already has a wallet")
    void shouldThrowExceptionWhenUserAlreadyHasWallet() {
        // Arrange
        CreateWalletCommand command = new CreateWalletCommand(userId);

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createWalletUseCase.execute(command));

        assertEquals("Wallet already exists for this userId", exception.getMessage());
        verify(walletRepositoryPort, times(1)).existsByUserId(userId);
        verify(walletRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should not create wallet when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> createWalletUseCase.execute(null));

        verify(walletRepositoryPort, never()).existsByUserId(any());
        verify(walletRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should not create wallet when userId is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> new CreateWalletCommand(null));
    }

    @Test
    @DisplayName("should save wallet with zero balance")
    void shouldSaveWalletWithZeroBalance() {
        // Arrange
        CreateWalletCommand command = new CreateWalletCommand(userId);

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(walletRepositoryPort.save(any())).thenReturn(Wallet.create(userId));

        // Act
        createWalletUseCase.execute(command);

        // Assert
        verify(walletRepositoryPort).save(argThat(wallet ->
                wallet.getBalance().equals(BigDecimal.ZERO)
        ));
    }
}
