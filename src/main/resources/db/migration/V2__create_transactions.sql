-- V2__create_transactions.sql
CREATE TABLE transactions (
                              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,           -- PK
                              sender_account_id BIGINT NULL,                           -- 출금 계좌, NULL 가능
                              receiver_account_id BIGINT NULL,                         -- 입금 계좌, NULL 가능
                              amount BIGINT NOT NULL,                                  -- 거래 금액
                              fee BIGINT NOT NULL DEFAULT 0,                           -- 수수료
                              type VARCHAR(20) NOT NULL,                               -- 거래 타입 (DEPOSIT, WITHDRAW, TRANSFER)
                              created_at DATETIME NOT NULL,                            -- 생성일
                              CONSTRAINT fk_sender_account FOREIGN KEY (sender_account_id)
                                  REFERENCES accounts(id),
                              CONSTRAINT fk_receiver_account FOREIGN KEY (receiver_account_id)
                                  REFERENCES accounts(id)
);