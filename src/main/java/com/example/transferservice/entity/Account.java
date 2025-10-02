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
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                     // DB PK

    @Column(nullable = false, unique = true)
    private String phoneNumber;          // 사용자 핸드폰 번호

    @Column(nullable = false, unique = true)
    private String accountNumber;        // 실제 계좌번호, 유니크

    @Column(nullable = false)
    private String ownerName;            // 계좌 소유자 이름

    @Column(nullable = false)
    private Long balance;                // 계좌 잔액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;        // 계좌 상태 (ACTIVE, INACTIVE, DELETED)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;     // 생성일

    @Column(nullable = false)
    private LocalDateTime updatedAt;     // 수정일

    @Version
    private Long version;                // 낙관적 락 버전

    public enum AccountStatus {
        ACTIVE,     // 활성 계좌
        INACTIVE,   // 비활성화 계좌
        DELETED     // 삭제된 계좌
    }

    // ========================================
    // 엔티티 생성/수정 전 처리
    // ========================================

    @PrePersist
    public void prePersist() {
        this.balance = (balance == null) ? 0L : balance;
        this.status = (status == null) ? AccountStatus.ACTIVE : status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}