package dev.cantrella.ms_wallet.infra.adapter.in.web;

import dev.cantrella.ms_wallet.application.dto.*;
import dev.cantrella.ms_wallet.application.port.*;
import dev.cantrella.ms_wallet.domain.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final ConsultBalanceUseCase consultBalanceUseCase;
    private final ConsultBalanceHistoryUseCase consultBalanceHistoryUseCase;

    @PostMapping(path = "wallets")
    ResponseEntity<CreateWalletResponse> createWallet(
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaim("email");
        UUID walletId = createWalletUseCase.execute(userEmail);
        return new ResponseEntity<>(
                new CreateWalletResponse(walletId.toString()),
                HttpStatus.CREATED);
    }

    @PostMapping(path = "wallets/deposit")
    ResponseEntity<Transaction> deposit(
            @RequestBody @Valid DepositOrWithdrawRequest depositOrWithdrawRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaim("email");
        DepositOrWithdrawCommand depositOrWithdrawCommand = new DepositOrWithdrawCommand(userEmail, depositOrWithdrawRequest.amount());
        Transaction transaction = depositUseCase.execute(depositOrWithdrawCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }
    @PostMapping(path = "wallets/withdraw")
    ResponseEntity<Transaction> withdraw(
            @RequestBody @Valid DepositOrWithdrawRequest depositOrWithdrawRequest,
            @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaim("email");
        DepositOrWithdrawCommand depositOrWithdrawCommand = new DepositOrWithdrawCommand(userEmail, depositOrWithdrawRequest.amount());
        Transaction transaction = withdrawUseCase.execute(depositOrWithdrawCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }

    @GetMapping(path = "wallets/balance")
    ResponseEntity<BalanceResponse> balance(
            @RequestParam(required = false) String at,
            @AuthenticationPrincipal Jwt jwt) {
        // TODO: user regex to validate at format yyyy-MM-ddTHH:mm:ss
        BalanceResponse balanceResponse;
        String userEmail = jwt.getClaim("email");
        if(at == null) {
            balanceResponse = consultBalanceUseCase.execute(new ConsultBalanceQuery(userEmail));
        } else {
            LocalDateTime dateTime = LocalDateTime.parse(at);
            LocalDateTime now = LocalDateTime.now();
            if (dateTime.isAfter(now)) {
                dateTime = now;
            }
            balanceResponse = consultBalanceHistoryUseCase.execute(new BalanceHistoryQuery(userEmail, dateTime));
        }

        return new ResponseEntity<>(balanceResponse,
                HttpStatus.OK);
    }

    @PostMapping(path = "/transfers")
    ResponseEntity<Transaction> transfer(
            @RequestBody @Valid TransferRequest transferRequest,
            @AuthenticationPrincipal Jwt jwt) {
        TransferCommand transferCommand = new TransferCommand(
                UUID.fromString(transferRequest.sourceWalletId()),
                UUID.fromString(transferRequest.destinationWalletId()),
                transferRequest.amount());
        Transaction transaction = transferUseCase.execute(transferCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }

}
