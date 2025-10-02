package com.example.transferservice.dto.response.transaction;

import com.example.transferservice.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long senderAccountId;
    private Long receiverAccountId;
    private Long amount;
    private Long fee;
    private String type;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .senderAccountId(tx.getSenderAccount() != null ? tx.getSenderAccount().getId() : null)
                .receiverAccountId(tx.getReceiverAccount() != null ? tx.getReceiverAccount().getId() : null)
                .amount(tx.getAmount())
                .fee(tx.getFee())
                .type(tx.getType().name())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}