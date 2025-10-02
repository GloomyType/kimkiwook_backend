package com.example.transferservice.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {

    @NotBlank(message = "계좌 소유자 이름은 필수입니다.")
    private String ownerName;

    @NotBlank(message = "핸드폰 번호는 필수입니다.")
    @Size(min = 10, max = 15, message = "핸드폰 번호 길이가 올바르지 않습니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}