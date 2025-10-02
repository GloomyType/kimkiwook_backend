package com.example.transferservice.repository;

import com.example.transferservice.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByPhoneNumber(String phoneNumber);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByIdAndStatus(Long id, Account.AccountStatus status);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.status = 'ACTIVE'")
    Optional<Account> findByIdWithOptimisticLock(Long id);
}