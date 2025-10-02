package com.example.transferservice.dto.request.transaction;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRequest {
    @NotNull(message = "계좌 ID는 필수입니다.")
    private Long accountId;

    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
    private Long amount;
}