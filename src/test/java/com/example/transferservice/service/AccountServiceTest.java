package com.example.transferservice.service;

import com.example.transferservice.dto.request.account.AccountRequest;
import com.example.transferservice.dto.response.account.AccountResponse;
import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Account.AccountStatus;
import com.example.transferservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountService(accountRepository);
        System.out.println("===== AccountServiceTest 시작 =====");
    }

    @Test
    void createAccount_success() {
        System.out.println("[TEST] createAccount_success 시작");

        AccountRequest req = AccountRequest.builder()
                .ownerName("홍길동")
                .phoneNumber("01011112222")
                .build();

        when(accountRepository.findByPhoneNumber(req.getPhoneNumber())).thenReturn(Optional.empty());
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        Account saved = Account.builder()
                .id(1L)
                .ownerName(req.getOwnerName())
                .phoneNumber(req.getPhoneNumber())
                .accountNumber("123-123-1234")
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse resp = accountService.createAccount(req);

        System.out.println("생성된 계좌 ID: " + resp.getId());
        System.out.println("전화번호: " + resp.getPhoneNumber());
        System.out.println("계좌번호: " + resp.getAccountNumber());
        System.out.println("잔액: " + resp.getBalance());

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getPhoneNumber()).isEqualTo("01011112222");

        System.out.println("[TEST] createAccount_success 완료 ✅");
    }

    @Test
    void createAccount_duplicatePhone_throws() {
        System.out.println("[TEST] createAccount_duplicatePhone_throws 시작");

        AccountRequest req = AccountRequest.builder()
                .ownerName("홍길동")
                .phoneNumber("01011112222")
                .build();

        when(accountRepository.findByPhoneNumber(req.getPhoneNumber()))
                .thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 등록된 휴대폰 번호입니다.");

        System.out.println("[TEST] createAccount_duplicatePhone_throws 완료 ✅");
    }

    @Test
    void deleteAccount_notFound_throws() {
        System.out.println("[TEST] deleteAccount_notFound_throws 시작");

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccountById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("계좌를 찾을 수 없습니다.");

        System.out.println("[TEST] deleteAccount_notFound_throws 완료 ✅");
    }

    @Test
    void deleteAccount_alreadyDeleted_throws() {
        System.out.println("[TEST] deleteAccount_alreadyDeleted_throws 시작");

        Account acc = Account.builder()
                .id(2L)
                .status(AccountStatus.DELETED)
                .build();
        when(accountRepository.findById(2L)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.deleteAccountById(2L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("이미 삭제되었거나 비활성화된 계좌입니다.");

        System.out.println("[TEST] deleteAccount_alreadyDeleted_throws 완료 ✅");
    }
}