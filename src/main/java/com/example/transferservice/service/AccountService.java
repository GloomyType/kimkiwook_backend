package com.example.transferservice.service;

import com.example.transferservice.dto.request.account.AccountRequest;
import com.example.transferservice.dto.response.account.AccountResponse;
import com.example.transferservice.entity.Account;
import com.example.transferservice.entity.Account.AccountStatus;
import com.example.transferservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    // ==============================
    // 상수
    // ==============================
    private static final String ERR_ACCOUNT_NOT_FOUND = "계좌를 찾을 수 없습니다.";
    private static final String ERR_DUPLICATE_PHONE = "이미 등록된 휴대폰 번호입니다.";
    private static final String ERR_INVALID_STATUS = "이미 삭제되었거나 비활성화된 계좌입니다.";

    // ==============================
    // Public API
    // ==============================

    /**
     * 계좌 생성
     */
    @Transactional
    public AccountResponse createAccount(AccountRequest dto) {
        // 이미 등록된 휴대폰 번호인지 체크
        accountRepository.findByPhoneNumber(dto.getPhoneNumber())
                .ifPresent(acc -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERR_DUPLICATE_PHONE);
                });

        // 유니크 계좌번호 생성
        String accountNumber = generateUniqueAccountNumber();

        // 계좌 엔티티 생성
        Account account = Account.builder()
                .phoneNumber(dto.getPhoneNumber())
                .ownerName(dto.getOwnerName())
                .accountNumber(accountNumber)
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        return AccountResponse.fromEntity(saved);
    }

    /**
     * DB에 존재하지 않는 유니크 계좌번호 생성
     */
    private String generateUniqueAccountNumber() {
        String accNum;
        do {
            accNum = String.format("%03d-%03d-%04d",
                    (int) (Math.random() * 1000),
                    (int) (Math.random() * 1000),
                    (int) (Math.random() * 10000));
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
    }

    /**
     * 계좌 삭제 (상태 변경)
     */
    @Transactional
    public boolean deleteAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERR_ACCOUNT_NOT_FOUND));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERR_INVALID_STATUS);
        }

        account.setStatus(AccountStatus.DELETED);
        accountRepository.save(account);
        return true;
    }

    /**
     * 계좌 조회 (전화번호 기준)
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByPhoneNumber(String phoneNumber) {
        Account account = accountRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ERR_ACCOUNT_NOT_FOUND));
        return AccountResponse.fromEntity(account);
    }
}