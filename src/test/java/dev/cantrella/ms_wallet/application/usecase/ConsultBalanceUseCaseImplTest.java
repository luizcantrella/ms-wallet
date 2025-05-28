package dev.cantrella.ms_wallet.application.usecase;

import dev.cantrella.ms_wallet.application.dto.BalanceResponse;
import dev.cantrella.ms_wallet.application.dto.ConsultBalanceQuery;
import dev.cantrella.ms_wallet.application.exception.WalletNotFoundException;
import dev.cantrella.ms_wallet.domain.model.Wallet;
import dev.cantrella.ms_wallet.ports.out.CachePort;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsultBalanceUseCaseImplTest {
    @Mock
    private  WalletRepositoryPort walletRepositoryPort;
    @Mock
    private  CachePort cachePort;
    @InjectMocks
    private ConsultBalanceUseCaseImpl consultBalanceUseCase;

    @Test
    @DisplayName("Should return balance from wallet found in repository")
    void shouldReturnBalanceFromWalletFoundInRepository() {
        String userId = "bob@mail.com";
        ConsultBalanceQuery query = new ConsultBalanceQuery(userId);
        Wallet wallet = Wallet.create(userId);
        when(cachePort.get(userId, BalanceResponse.class)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wallet));
        doNothing().when(cachePort).put(any(), any(BalanceResponse.class));

        BalanceResponse balanceResponse = consultBalanceUseCase.execute(query);

        assertEquals(wallet.getBalance(), balanceResponse.balance());
        assertEquals(wallet.getId(), balanceResponse.walletId());
        verify(cachePort, times(1)).get(any(), any());
        verify(cachePort).get(userId, BalanceResponse.class);
        verify(cachePort).put(userId, balanceResponse);
        verify(cachePort, times(1)).put(any(), any());
    }

    @Test
    @DisplayName("Should throw an exception when don't find the wallet")
    void shouldThrowExceptionWhenDoNotFindWallet() {
        String userId = "bob@mail.com";
        ConsultBalanceQuery query = new ConsultBalanceQuery(userId);
        Wallet wallet = Wallet.create(userId);
        when(cachePort.get(userId, BalanceResponse.class)).thenReturn(null);
        when(walletRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

        var walletNotFoundException = assertThrows(WalletNotFoundException.class, () -> {
                consultBalanceUseCase.execute(query);
        });

        assertEquals(userId, walletNotFoundException.getUserId());
        assertEquals("Wallet for user ID " + userId + " not found", walletNotFoundException.getMessage());
        verify(cachePort, times(1)).get(any(), any());
        verify(cachePort).get(userId, BalanceResponse.class);
        verify(cachePort, never()).put(any(), any());
    }
}
