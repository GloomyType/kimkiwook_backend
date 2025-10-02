package com.example.transferservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // DB PK

    // ========================================
    // 계좌 정보
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account senderAccount;       // 출금 계좌 (출금 시 필수, 입금 시 null 가능)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id")
    private Account receiverAccount;     // 입금 계좌 (입금 시 필수, 출금 시 null 가능)

    // ========================================
    // 거래 정보
    // ========================================

    @Column(nullable = false)
    private Long amount;                 // 거래 금액

    @Column(nullable = false)
    private Long fee = 0L;               // 수수료, 기본 0, 이체 시 1%

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;        // 거래 타입 (DEPOSIT, WITHDRAW, TRANSFER)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;     // 생성일

    // ========================================
    // 엔티티 생성 전 처리
    // ========================================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ========================================
    // 거래 타입
    // ========================================

    public enum TransactionType {
        DEPOSIT,    // 입금
        WITHDRAW,   // 출금
        TRANSFER    // 이체
    }
}