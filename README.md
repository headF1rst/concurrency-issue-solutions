# Concurrency Issue Solutions

동시성 이슈란 무엇인지 알아보고 처리하는 방법들을 학습하기 위한 공간입니다.

## Solutions

### 1. Application Level

- `synchronized` 키워드를 사용하여 문제 해결

### 2. Database Lock
데이터베이스가 제공하는 Lock을 이용하여 동시성을 제어.

- Pessimistic Lock (비관적 락)
- Optimistic Lock (낙관적 락)
- Name Lock (네임 락)

### 3. Redis Distributed Lock
Redis의 기능을 활용해 동시성을 제어.

- 라이브러리 비교
- Redis Cli를 통한 명령어 실습
- Lettuce 활용
- Redisson 활용
