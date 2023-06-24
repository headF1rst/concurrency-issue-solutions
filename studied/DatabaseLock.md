# Database를 활용해서 데이터 정합성 맞추는 여러가지 방법

## 분산락이란?

여러 서버에서 공유되는 데이터를 제어하기 위해 사용하는 기술.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbMsxpO%2Fbtr93lN48RZ%2FkVlt6Gw6FjIf3mkpQobui0%2Fimg.png)
<center><small>출처 - https://llshl.tistory.com/92 </small></center>

하나의 데이터베이스에 여러 인스턴스가 동시에 접근하면 데이터 정합성이 깨질 수 있다. 데이터베이스에 락을 걸어주어 한번에 하나의
서버에서만 자원에 접근하도록 제어하는것이 분산락이다.

## 1. Pessimistic Lock (비관적 락)

- 실제로 데이터에 락을 걸어서 정합성을 맞추는 방법.
- exclusive lock을 걸게되면 다른 트랜잭션에서는 lock이 해제되기전에 데이터를 가져갈 수 없게 된다.
- 데드락이 발생할 가능성이 있기 때문에 주의해서 사용해야 한다.

## 2. Optimistic Lock (낙관적 락)

- 실제로 락을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법.
- 먼저 데이터를 읽은 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인한 다음. 버전을 업데이트 한다. (ex. 버전 + 1)
- 내가 읽은 버전에서 수정 사항이 생겼을 경우에는 application에서 다시 읽은 후에 작업을 수행해야 한다.

## 3. Named Lock

- 이름을 가진 metadata locking. 이름을 가진 락을 획득한 후 해제할때 까지 다른 세션은 이 락을 획득할 수 없도록 한다.
- 주의할점은 트랜잭션이 종료될 때 lock이 자동으로 해제되지 않는다. 
  - 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제된다.
- MySQL에서는 get_lock 명령어를 통해 네임드 락을 획득할 수 있고 release 명령어를 통해 해제할 수 있다.

---

## Pessimistic Lock을 사용한 동시성 문제 해결

Spring Data JPA에서는 `@Lock` 어노테이션을 통해서 Pessimistic lock을 적용할 수 있다.

**장점**
- 충돌이 빈번하게 일어나는 케이스에 적용하면 Optimistic lock보다 성능이 좋을 수 있다.
- 락을 통해 업데이트를 제어하기 때문에 데이터 정합성을 어느정도 보장할 수 있다.

**단점**
- 별도의 락을 잡기 때문에 성능 감소가 있을수 있다.

## Optimistic Lock을 사용한 동시성 문제 해결

Optimistic lock을 사용하기 위해서는 버전을 명시하는 version 필드가 필요하며 `@Version` 어노테이션을 설정해 줘야한다.

**장점**
- 별도의 락을 잡지 않으므로 Pessimistic lock보다 성능이 뛰어나다.

**단점**
- 업데이트 실패시 재시도 로직을 개발자가 직접 작성해줘야한다.

## Named Lock을 사용한 동시성 문제 해결

**장점**
- Named Lock은 주로 분산 락을 구현할때 사용된다.
- 데이터 삽입시에 정합성을 맞춰야하는 케이스에도 유용하게 사용할 수 있다.

**단점**
- 구현이 복잡하다
- 별도의 명령어로 락을 해제해줘야 한다.
