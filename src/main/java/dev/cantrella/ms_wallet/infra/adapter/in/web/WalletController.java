package dev.cantrella.ms_wallet.infra.adapter.in.web;

import dev.cantrella.ms_wallet.application.dto.*;
import dev.cantrella.ms_wallet.application.port.*;
import dev.cantrella.ms_wallet.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ResponseEntity<CreateWalletResponse> createWallet(@RequestBody CreateWalletRequest createWalletRequest) {
        CreateWalletCommand walletCommand = new CreateWalletCommand(createWalletRequest.userId());
        UUID walletId = createWalletUseCase.execute(walletCommand);
        return new ResponseEntity<>(
                new CreateWalletResponse(walletId.toString()),
                HttpStatus.CREATED);
    }

    @PostMapping(path = "wallets/{walletId}/deposit")
    ResponseEntity<Transaction> deposit(
            @PathVariable UUID walletId,
            @RequestBody DepositRequest depositRequest) {
        DepositCommand depositCommand = new DepositCommand(walletId, depositRequest.amount());
        Transaction transaction = depositUseCase.execute(depositCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }
    @PostMapping(path = "wallets/{walletId}/withdraw")
    ResponseEntity<Transaction> withdraw(
            @PathVariable UUID walletId,
            @RequestBody DepositRequest depositRequest) {
        DepositCommand depositCommand = new DepositCommand(walletId, depositRequest.amount());
        Transaction transaction = withdrawUseCase.execute(depositCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }

    @GetMapping(path = "wallets/{walletId}/balance")
    ResponseEntity<BalanceResponse> balance(
            @PathVariable("walletId") UUID walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at) {
        BalanceResponse balanceResponse;
        if(at == null) {
            balanceResponse = consultBalanceUseCase.execute(new ConsultBalanceQuery(walletId));
        } else {
            balanceResponse = consultBalanceHistoryUseCase.execute(new BalanceHistoryQuery(walletId, at));
        }

        return new ResponseEntity<>(balanceResponse,
                HttpStatus.OK);
    }

    @GetMapping(path = "wallets/{walletId}/balance/history")
    ResponseEntity<BalanceResponse> history(
            @PathVariable("walletId") UUID walletId,
            @RequestParam String at) {
        // convert at to LocalDateTime
        LocalDateTime dataHora = LocalDateTime.parse(at);
        BalanceResponse balanceResponse = consultBalanceHistoryUseCase
                .execute(new BalanceHistoryQuery(walletId, dataHora));

        return new ResponseEntity<>(balanceResponse,
                HttpStatus.OK);
    }

    @PostMapping(path = "/transfers")
    ResponseEntity<Transaction> transfer(
            @RequestBody TransferRequest transferRequest) {
        TransferCommand transferCommand = new TransferCommand(
                UUID.fromString(transferRequest.sourceWalletId()),
                UUID.fromString(transferRequest.destinationWalletId()),
                transferRequest.amount());
        Transaction transaction = transferUseCase.execute(transferCommand);
        return new ResponseEntity<>(transaction,
                HttpStatus.OK);
    }

}
