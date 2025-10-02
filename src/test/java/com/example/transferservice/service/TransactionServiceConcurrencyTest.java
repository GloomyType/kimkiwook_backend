package com.example.transferservice.service;

import com.example.transferservice.dto.request.transaction.WithdrawRequest;
import com.example.transferservice.dto.request.transaction.TransferRequest;
import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Account.AccountStatus;
import com.example.transferservice.repository.AccountRepository;
import com.example.transferservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        senderAccount = Account.builder()
                .phoneNumber("01011112222")
                .ownerName("송금자")
                .balance(1_000_000L)
                .status(AccountStatus.ACTIVE)
                .accountNumber("111-111-1111")
                .build();

        receiverAccount = Account.builder()
                .phoneNumber("01033334444")
                .ownerName("수취자")
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .accountNumber("222-222-2222")
                .build();

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);
    }

    @Test
    void concurrentWithdrawTest() throws InterruptedException {
        int threadCount = 5;
        long withdrawAmount = 300_000L;

        System.out.println("===== 동시 출금 테스트 시작 =====");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            futures.add(executor.submit(() -> {
                try {
                    transactionService.withdraw(new WithdrawRequest(senderAccount.getId(), withdrawAmount));
                    System.out.println("[Thread " + idx + "] 출금 성공: " + withdrawAmount);
                    return true;
                } catch (Exception e) {
                    System.out.println("[Thread " + idx + "] 출금 실패: " + e.getMessage());
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();
        executor.shutdown();

        long successCount = futures.stream().filter(f -> {
            try { return f.get(); } catch (Exception e) { return false; }
        }).count();

        Account updated = accountRepository.findById(senderAccount.getId()).orElseThrow();
        long totalWithdrawn = successCount * withdrawAmount;

        System.out.println("총 출금 성공 횟수: " + successCount);
        System.out.println("최종 계좌 잔액: " + updated.getBalance());
        System.out.println("출금 총액: " + totalWithdrawn);
        System.out.println("===== 동시 출금 테스트 종료 =====");

        assertThat(updated.getBalance()).isEqualTo(1_000_000L - totalWithdrawn);
        assertThat(updated.getBalance()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void concurrentTransferTest() throws InterruptedException {
        int threadCount = 5;
        long transferAmount = 500_000L;

        System.out.println("===== 동시 이체 테스트 시작 =====");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i + 1;
            futures.add(executor.submit(() -> {
                try {
                    transactionService.transfer(new TransferRequest(senderAccount.getId(), receiverAccount.getId(), transferAmount));
                    System.out.println("[Thread " + idx + "] 이체 성공: " + transferAmount);
                    return true;
                } catch (Exception e) {
                    System.out.println("[Thread " + idx + "] 이체 실패: " + e.getMessage());
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();
        executor.shutdown();

        long successCount = futures.stream().filter(f -> {
            try { return f.get(); } catch (Exception e) { return false; }
        }).count();

        Account updatedSender = accountRepository.findById(senderAccount.getId()).orElseThrow();
        Account updatedReceiver = accountRepository.findById(receiverAccount.getId()).orElseThrow();

        long feePer = Math.round(transferAmount * 0.01);
        long totalSent = successCount * transferAmount;
        long totalFee = successCount * feePer;

        System.out.println("총 이체 성공 횟수: " + successCount);
        System.out.println("송금자 최종 잔액: " + updatedSender.getBalance());
        System.out.println("수취자 최종 잔액: " + updatedReceiver.getBalance());
        System.out.println("총 송금액: " + totalSent + ", 총 수수료: " + totalFee);
        System.out.println("===== 동시 이체 테스트 종료 =====");

        assertThat(updatedSender.getBalance()).isEqualTo(1_000_000L - totalSent - totalFee);
        assertThat(updatedReceiver.getBalance()).isEqualTo(totalSent);
    }
}