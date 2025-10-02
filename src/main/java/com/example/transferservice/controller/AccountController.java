package com.example.transferservice.controller;

import com.example.transferservice.dto.request.account.AccountRequest;
import com.example.transferservice.dto.response.account.AccountResponse;
import com.example.transferservice.dto.response.common.ResponseData;
import com.example.transferservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성", description = "핸드폰 인증(생략) 후 계좌를 생성합니다.")
    @PostMapping
    public ResponseData<AccountResponse> createAccount(@Valid @RequestBody AccountRequest dto) {
        AccountResponse accountResponse = accountService.createAccount(dto);
        return ResponseData.of(accountResponse);
    }

    @Operation(summary = "계좌 삭제", description = "계좌 상태를 DELETED로 변경합니다.")
    @DeleteMapping("/{accountId}")
    public ResponseData<Map<String, String>> deleteAccount(@PathVariable Long accountId) {
        boolean deleted = accountService.deleteAccountById(accountId);

        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다.");
        }

        Map<String, String> response = Map.of("message", "계좌 삭제 성공");
        return ResponseData.of(response);
    }

    @Operation(summary = "계좌 조회", description = "전화번호로 계좌를 조회합니다.")
    @GetMapping("/{phoneNumber}")
    public ResponseData<AccountResponse> getAccountByPhone(@PathVariable String phoneNumber) {
        AccountResponse accountResponse = accountService.getAccountByPhoneNumber(phoneNumber);
        return ResponseData.of(accountResponse);
    }
}