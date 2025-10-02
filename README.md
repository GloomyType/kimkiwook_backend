# Transfer Service (송금 서비스)

## 프로젝트 개요
이 프로젝트는 계좌 등록, 입금, 출금, 이체 기능을 제공하는 송금 서비스입니다.  
사용자는 먼저 계좌를 생성하고, 생성한 계좌를 기반으로 **모든 거래(입금, 출금, 계좌 간 이체)**를 수행할 수 있습니다.  
테스트용으로 생성된 계좌끼리만 이체가 가능하며, 실제 환경에서는 다른 계좌로도 이체가 가능합니다.  
서비스는 **Spring Boot + JPA + MySQL + Docker Compose** 환경에서 동작하도록 설계되었습니다.

## 확장성 고려
- **모듈화**: 엔티티, 서비스, 리포지토리, 예외가 계층별로 분리되어 확장 용이.
- **멀티모듈 아키텍처 느낌**: 각 도메인(Account/Transaction) 별로 별도 모듈로 분리 가능.
- **인프라 변경 용이**:
   - DB 변경 시 Repository만 수정 가능, 서비스/비즈니스 로직은 최소 수정.
   - 웹 프레임워크 변경(Spring Boot → NestJS 등) 시에도 서비스/비즈니스 로직 대부분 재사용 가능.
- **동시성 문제 대응**: Optimistic Lock 적용으로 다중 트랜잭션 안정화.

# 프로젝트 실행 가이드

## 1. 준비 사항
1. **Docker Desktop 설치 및 실행**
    - [Docker Desktop 다운로드](https://www.docker.com/products/docker-desktop)
    - 설치 후 Docker Desktop 실행

2. **IntelliJ 설치 및 JDK 17 이상 설정**
    - IntelliJ IDEA 설치
    - JDK 17 이상 설정 확인

## 2. Docker Compose 실행
- 프로젝트 루트에 `docker-compose.yaml` 파일이 있어야 합니다.
- 터미널에서 다음 명령어를 실행하여 컨테이너를 시작:

```bash
docker-compose up -d
```

## 3. 컨테이너 실행상태 확인

```bash
docker-compose ps
```

## 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

## 5. 스웨거 접속
- 서버 실행 후 브라우저에서 Swagger UI 접속
- http://localhost:8080/swagger-ui/index.html

## 6. 서버 종료 명령

```bash
docker-compose down -v
kill -9 $(ps aux | grep '[b]ootRun' | awk '{print $2}')
```

# 테스트 실행 방법

본 프로젝트에서는 다음과 같은 단위 및 통합 테스트를 제공합니다. 각 테스트의 목적과 Gradle 실행 명령어를 안내합니다.

---

## 1. AccountServiceTest
계좌 생성, 중복 계좌 예외, 삭제 예외 등 **계좌 서비스 관련 단위 테스트**.

```bash
./gradlew test --tests "com.example.transferservice.service.AccountServiceTest" --rerun-tasks
```

검증 항목
- 정상 계좌 생성
- 중복 전화번호 등록 시 예외 발생
- 존재하지 않는 계좌 삭제 시 예외 발생
- 이미 삭제된 계좌 삭제 시 예외 발생


## 2. TransactionServiceUnitTest
출금, 송금, 수수료, 잔액, 예외 처리 등 **단일 스레드, 로직/예외 검증**.

```bash
./gradlew test --tests "com.example.transferservice.service.TransactionServiceUnitTest" --rerun-tasks
```

검증 항목
- 출금 시 잔액 부족 예외
- 정상 출금 처리 및 트랜잭션 생성
- 송금 처리 및 수수료 계산
- 송금자 계좌 미존재 시 예외 발생

## 3. TransactionServiceConcurrencyTest
출금, 송금, 수수료, 잔액, 예외 처리 등 **다중 스레드, 동시성/충돌 검증**.

```bash
./gradlew test --tests "com.example.transferservice.service.TransactionServiceConcurrencyTest" --rerun-tasks
```

검증 항목
- 여러 스레드가 동시에 출금 시 최종 잔액 검증
- 여러 스레드가 동시에 송금 시 최종 잔액 및 수수료 검증
- 동시성 환경에서 데이터 충돌 처리 확인

## API 명세서 : https://www.notion.so/API-27e4c7d817c680fb989de1f21377dc37
