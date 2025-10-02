package com.example.transferservice.repository;

import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderAccountOrReceiverAccountOrderByCreatedAtDesc(Account sender, Account receiver);
    List<Transaction> findAllByOrderByCreatedAtDesc();

    // 출금 합계 (오늘 기준)
    @Query("SELECT COALESCE(SUM(t.amount + t.fee), 0) " +
            "FROM Transaction t " +
            "WHERE t.senderAccount = :account " +
            "AND t.type = 'WITHDRAW' " +
            "AND t.createdAt >= :startOfDay " +
            "AND t.createdAt <= :endOfDay")
    long sumWithdrawalsToday(@Param("account") Account account,
                             @Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    // 이체 합계 (오늘 기준)
    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.senderAccount = :account " +
            "AND t.type = 'TRANSFER' " +
            "AND t.createdAt >= :startOfDay " +
            "AND t.createdAt <= :endOfDay")
    long sumTransfersToday(@Param("account") Account account,
                           @Param("startOfDay") LocalDateTime startOfDay,
                           @Param("endOfDay") LocalDateTime endOfDay);
}