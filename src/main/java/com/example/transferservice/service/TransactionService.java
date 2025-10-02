package com.example.transferservice.service;

import com.example.transferservice.dto.request.transaction.DepositRequest;
import com.example.transferservice.dto.request.transaction.WithdrawRequest;
import com.example.transferservice.dto.request.transaction.TransferRequest;
import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Transaction;
import com.example.transferservice.entity.Transaction.TransactionType;
import com.example.transferservice.exception.AccountNotFoundException;
import com.example.transferservice.exception.InsufficientBalanceException;
import com.example.transferservice.repository.AccountRepository;
import com.example.transferservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ==============================
    // 상수
    // ==============================
    private static final String ERR_ACCOUNT_NOT_FOUND = "계좌를 찾을 수 없습니다.";
    private static final String ERR_INSUFFICIENT_BALANCE = "잔액이 부족합니다.";
    private static final String ERR_DAILY_WITHDRAW_LIMIT = "일일 출금 한도를 초과했습니다. (최대 %d원)";
    private static final String ERR_DAILY_TRANSFER_LIMIT = "일일 이체 한도를 초과했습니다. (최대 %d원)";

    private static final long DAILY_WITHDRAW_LIMIT = 1_000_000L;
    private static final long DAILY_TRANSFER_LIMIT = 3_000_000L;

    // ==============================
    // Public API
    // ==============================

    /**
     * 계좌 입금
     */
    @Transactional
    public Transaction deposit(DepositRequest request) {
        Account account = findAccountOrThrow(request.getAccountId());
        account.setBalance(account.getBalance() + request.getAmount());
        accountRepository.save(account);
        return createTransaction(null, account, request.getAmount(), 0L, TransactionType.DEPOSIT);
    }

    /**
     * 계좌 출금 (일일 한도 체크 + 낙관적 락)
     */
    @Transactional
    public Transaction withdraw(WithdrawRequest request) {
        Account account = accountRepository.findByIdWithOptimisticLock(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND));

        validateSufficientBalance(account, request.getAmount());
        validateDailyWithdrawLimit(account, request.getAmount());

        account.setBalance(account.getBalance() - request.getAmount());
        accountRepository.save(account);

        return createTransaction(account, null, request.getAmount(), 0L, TransactionType.WITHDRAW);
    }

    /**
     * 계좌 이체 (1% 수수료 + 일일 한도 체크)
     */
    @Transactional
    public Transaction transfer(TransferRequest request) {
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new IllegalArgumentException("출금 계좌와 수취 계좌가 동일합니다.");
        }

        Account sender = accountRepository.findByIdWithOptimisticLock(request.getSenderId())
                .orElseThrow(() -> new AccountNotFoundException("송금자 " + ERR_ACCOUNT_NOT_FOUND));

        Account receiver = findAccountOrThrow(request.getReceiverId());

        long fee = calculateFee(request.getAmount(), 0.01); // 1% 수수료
        long totalDebit = request.getAmount() + fee;

        validateSufficientBalance(sender, totalDebit);
        validateDailyTransferLimit(sender, request.getAmount());

        sender.setBalance(sender.getBalance() - totalDebit);
        receiver.setBalance(receiver.getBalance() + request.getAmount());

        accountRepository.save(sender);
        accountRepository.save(receiver);

        return createTransaction(sender, receiver, request.getAmount(), fee, TransactionType.TRANSFER);
    }

    /**
     * 특정 계좌 거래 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(Long accountId) {
        Account account = findAccountOrThrow(accountId);
        return transactionRepository.findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(account, account);
    }

    /**
     * 전체 거래 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==============================
    // Utility Methods
    // ==============================

    private Account findAccountOrThrow(Long accountId) {
        return accountRepository.findByIdAndStatus(accountId, Account.AccountStatus.ACTIVE)
                .orElseThrow(() -> new AccountNotFoundException(ERR_ACCOUNT_NOT_FOUND));
    }

    private void validateSufficientBalance(Account account, long requiredAmount) {
        if (account.getBalance() < requiredAmount) {
            throw new InsufficientBalanceException(ERR_INSUFFICIENT_BALANCE);
        }
    }

    private long calculateFee(long amount, double rate) {
        return Math.round(amount * rate);
    }

    private void validateDailyWithdrawLimit(Account account, long amount) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        long sumToday = transactionRepository.sumWithdrawalsToday(account, startOfDay, endOfDay);

        if (sumToday + amount >= DAILY_WITHDRAW_LIMIT) {
            throw new InsufficientBalanceException(String.format(ERR_DAILY_WITHDRAW_LIMIT, DAILY_WITHDRAW_LIMIT));
        }
    }

    private void validateDailyTransferLimit(Account account, long amount) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        long sumToday = transactionRepository.sumTransfersToday(account, startOfDay, endOfDay);

        if (sumToday + amount >= DAILY_TRANSFER_LIMIT) {
            throw new InsufficientBalanceException(String.format(ERR_DAILY_TRANSFER_LIMIT, DAILY_TRANSFER_LIMIT));
        }
    }

    private Transaction createTransaction(Account sender, Account receiver,
                                          long amount, long fee, TransactionType type) {
        Transaction tx = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .fee(fee)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();
        return transactionRepository.save(tx);
    }
}