CREATE TABLE accounts (
                          id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          phone_number VARCHAR(255) NOT NULL UNIQUE,     -- 사용자 핸드폰 번호
                          account_number VARCHAR(20) NOT NULL UNIQUE,    -- 실제 계좌번호
                          owner_name VARCHAR(255) NOT NULL,
                          balance BIGINT NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at DATETIME NOT NULL,
                          updated_at DATETIME NOT NULL,
                          version BIGINT
);