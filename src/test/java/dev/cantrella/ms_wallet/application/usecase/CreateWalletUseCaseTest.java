package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.domain.model.Wallet;
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

    private String userId="john.doe@mail.com";

    @BeforeEach
    void startUp() {
        userId = UUID.randomUUID().toString();
    }
    @Test
    @DisplayName("should create new wallet when user has no wallet")
    void shouldCreateNewWalletWhenUserHasNoWallet() {

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(walletRepositoryPort.save(any())).thenReturn(Wallet.create(userId));

        createWalletUseCase.execute(userId);

        verify(walletRepositoryPort, times(1)).existsByUserId(userId);
        verify(walletRepositoryPort, times(1)).save(argThat(wallet ->
                wallet.getUserId().equals(userId) &&
                        wallet.getBalance().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    @DisplayName("should throw exception when user already has a wallet")
    void shouldThrowExceptionWhenUserAlreadyHasWallet() {

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createWalletUseCase.execute(userId));

        assertEquals("Wallet already exists", exception.getMessage());
        verify(walletRepositoryPort, times(1)).existsByUserId(userId);
        verify(walletRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should not create wallet when command is null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> createWalletUseCase.execute(null));

        verify(walletRepositoryPort, never()).existsByUserId(any());
        verify(walletRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should save wallet with zero balance")
    void shouldSaveWalletWithZeroBalance() {

        when(walletRepositoryPort.existsByUserId(userId)).thenReturn(false);
        when(walletRepositoryPort.save(any())).thenReturn(Wallet.create(userId));

        createWalletUseCase.execute(userId);

        verify(walletRepositoryPort).save(argThat(wallet ->
                wallet.getBalance().equals(BigDecimal.ZERO)
        ));
    }
}
