package com.example.transferservice.dto.response.account;

import com.example.transferservice.entity.Account;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String phoneNumber;
    private String accountNumber;
    private String ownerName;
    private Long balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse fromEntity(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .phoneNumber(account.getPhoneNumber())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}