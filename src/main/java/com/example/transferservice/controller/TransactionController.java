package com.example.transferservice.controller;

import com.example.transferservice.dto.request.transaction.DepositRequest;
import com.example.transferservice.dto.request.transaction.WithdrawRequest;
import com.example.transferservice.dto.request.transaction.TransferRequest;
import com.example.transferservice.dto.response.common.ResponseData;
import com.example.transferservice.dto.response.transaction.TransactionResponse;
import com.example.transferservice.entity.Transaction;
import com.example.transferservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "입금", description = "특정 계좌에 금액을 입금합니다.")
    @PostMapping("/deposit")
    public ResponseData<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        Transaction transaction = transactionService.deposit(request);
        return ResponseData.of(TransactionResponse.fromEntity(transaction));
    }

    @Operation(summary = "출금", description = "특정 계좌에서 금액을 출금합니다.")
    @PostMapping("/withdraw")
    public ResponseData<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Transaction transaction = transactionService.withdraw(request);
        return ResponseData.of(TransactionResponse.fromEntity(transaction));
    }

    @Operation(summary = "이체", description = "계좌 간 금액을 송금합니다.")
    @PostMapping("/transfer")
    public ResponseData<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transfer(request);
        return ResponseData.of(TransactionResponse.fromEntity(transaction));
    }

    @Operation(summary = "거래 내역 조회", description = "특정 계좌의 거래 내역 조회 또는 전체 거래 조회")
    @GetMapping
    public ResponseData<List<TransactionResponse>> getTransactionHistory(
            @RequestParam(value = "accountId", required = false) Long accountId) {

        List<Transaction> transactions;

        if (accountId != null) {
            transactions = transactionService.getTransactions(accountId);
        } else {
            transactions = transactionService.getAllTransactions();
        }

        List<TransactionResponse> responseList = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseData.of(responseList);
    }
}