package com.example.transferservice.service;

import com.example.transferservice.dto.request.transaction.TransferRequest;
import com.example.transferservice.dto.request.transaction.WithdrawRequest;
import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Transaction;
import com.example.transferservice.entity.Transaction.TransactionType;
import com.example.transferservice.exception.AccountNotFoundException;
import com.example.transferservice.exception.InsufficientBalanceException;
import com.example.transferservice.repository.AccountRepository;
import com.example.transferservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceUnitTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionService(accountRepository, transactionRepository);
    }

    @Test
    void withdraw_insufficientBalance_throws() {
        Account account = Account.builder()
                .id(1L)
                .balance(100L)
                .status(Account.AccountStatus.ACTIVE)
                .build();
        when(accountRepository.findByIdWithOptimisticLock(1L)).thenReturn(Optional.of(account));

        WithdrawRequest req = new WithdrawRequest(1L, 200L);

        System.out.println("테스트: 출금 금액이 잔액보다 큰 경우");
        assertThatThrownBy(() -> transactionService.withdraw(req))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("잔액이 부족합니다.");
        System.out.println("✅ 예외 발생 확인 완료");
    }

    @Test
    void withdraw_success_createsTransaction() {
        Account account = Account.builder()
                .id(1L)
                .balance(1000L)
                .status(Account.AccountStatus.ACTIVE)
                .build();
        when(accountRepository.findByIdWithOptimisticLock(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(10L);
                    return t;
                });

        WithdrawRequest req = new WithdrawRequest(1L, 500L);
        Transaction tx = transactionService.withdraw(req);

        System.out.println("테스트: 정상 출금 처리");
        System.out.println("출금 트랜잭션 ID: " + tx.getId());
        System.out.println("남은 잔액: " + account.getBalance());

        assertThat(tx).isNotNull();
        assertThat(tx.getType()).isEqualTo(TransactionType.WITHDRAW);
        verify(accountRepository).save(account);
        assertThat(account.getBalance()).isEqualTo(500L);
    }

    @Test
    void transfer_feeAndLimits_andBalances() {
        Account sender = Account.builder().id(1L).balance(1_000_000L).status(Account.AccountStatus.ACTIVE).build();
        Account receiver = Account.builder().id(2L).balance(0L).status(Account.AccountStatus.ACTIVE).build();

        when(accountRepository.findByIdWithOptimisticLock(1L)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdAndStatus(2L, Account.AccountStatus.ACTIVE)).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest(1L, 2L, 100_000L);

        Transaction tx = transactionService.transfer(req);

        long fee = Math.round(100_000L * 0.01);

        System.out.println("테스트: 송금 처리");
        System.out.println("송금 금액: " + tx.getAmount());
        System.out.println("수수료: " + tx.getFee());
        System.out.println("송금자 잔액: " + sender.getBalance());
        System.out.println("수취자 잔액: " + receiver.getBalance());

        assertThat(tx.getFee()).isEqualTo(fee);
        assertThat(tx.getAmount()).isEqualTo(100_000L);
        assertThat(sender.getBalance()).isEqualTo(1_000_000L - 100_000L - fee);
        assertThat(receiver.getBalance()).isEqualTo(100_000L);
        verify(accountRepository).save(sender);
        verify(accountRepository).save(receiver);
    }

    @Test
    void transfer_senderNotFound_throws() {
        when(accountRepository.findByIdWithOptimisticLock(1L)).thenReturn(Optional.empty());
        TransferRequest req = new TransferRequest(1L, 2L, 100L);

        System.out.println("테스트: 송금자 계좌가 존재하지 않을 때");
        assertThatThrownBy(() -> transactionService.transfer(req))
                .isInstanceOf(AccountNotFoundException.class);
        System.out.println("✅ 예외 발생 확인 완료");
    }
}