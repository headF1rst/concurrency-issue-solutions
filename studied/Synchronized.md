# Synchronized 사용하여 동시성 문제 해결하기

이전 Race Condition은 둘 이상의 스레드가 공유 자원에 동시에 접근하려 했기 때문에 발생하는 문제였다.
`Synchronized` 키워드를 사용하여 공유 자원에 한번에 한 개의 스레드만 접근 가능하게 하면 문제를 해결할 수 있다.

코틀린에서 synchronized를 메서드에 사용하려면 `@Synchronized` 어노테이션 표기법을 사용해야한다.
![](https://i.imgur.com/pqCTlLp.png)

하지만 비동기로 100건의 요청을 여러 스레드가 동시에 처리하는 테스트를 수행해 본 결과, 여전히 테스트는 실패했다...

![](https://i.imgur.com/seDNSiT.png)
Synchronized를 사용했음에도 테스트가 실패하는 이유는 Spring의 `@Trasactional` 동작 방식 때문이다.
@Transactional은 지정된 클래스 혹은 메서드에 대해 내부적으로 AOP를 통해 트랜잭션 처리코드를 전 후로 수행한다.

## AOP (Aspect Oriented Programming)

AOP에 대해 간략하게 설명하자면 핵심 기능 코드에 존재하는 공통된 부가기능 코드를 독립적으로 분리해주는 기술이다.
핵심 비즈니스 로직이 아닌 로깅, 트랜잭션과 같은 부가기능을 AOP를 통해 핵심 로직으로 부터 분리함으로써 객체의 책임이 명확해지고 개발자는 
비즈니스 로직에만 집중할 수 있게된다. 즉, OOP를 돕는 보조 기술이다.

### AOP의 두가지 방식

- JDK Dynamic Proxy 방식
- CGLib 방식

### JDK Dynamic Proxy

타깃 오브젝트가 상속하는 인터페이스를 상속하여 추상 메서드를 구현한 프록시 객체를 생성한다.
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F1c2sx%2Fbtq5MoWVYP7%2FXSZ2PjIbxEIPKE2KKHYVn0%2Fimg.png)
<center><small>출처 - https://hwannny.tistory.com/98</small></center>

왼쪽은 @Transactional을 적용하기 전이며, 오른쪽은 @Transactional이 적용되고 트랜잭션 처리에 대한 책임이 프록시 객체에 위임된 상태이다.
클라이언트(Controller)는 프록시 메서드를 호출하며 프록시 객체는 내부적으로 타깃 메서드 호출 전,후로 트랜잭션 처리를 수행한다.

AOP는 프록시 패턴으로 클라이언트의 타깃 메서드에 대한 접근을 제어하고 부가 기능을 필요에 맞게 데코레이션 할 수 있는 데코레이터 패턴을 적용한 것이다.

JDK Dynamic Proxy 방식은 자바의 리플랙션 패키지에 존재하는 프록시 클래스를 통해 동적으로 프록시 객체를 생성한다.

**타깃 오브젝트가 인터페이스를 상속하지 않는다면? -> CGLib 방식을 사용하여 프록시 객체를 생성한다**

### CGLib

바이트코드 생성 프레임워크로 런타임에 타깃 오브젝트를 상속하는 프록시 객체를 생성한다.

## 테스트 실패 원인

Synchronized 키워드를 사용했음에도 동시성 테스트가 실패하는 이유는 Transactional 어노테이션으로 인해 프록시 객체가 생성되기 때문이다.

```kotlin
class TestServiceProxy(
    val testService: TestService,
) {
    fun test() {
        try {
            transaction.start()
            testService.decrease()
        } catch(Exception e) {
            transaction.rollback()
        } finally {
            transaction.commit()
        }
    }
}

class TestService {
    
    @Synchronized
    fun decrease() {
        // 수량 감소
    }
}
```

TestService는 상속받는 인터페이스가 없기 때문에 CGLib 방식으로 프록시 객체를 생성한다. 프록시 객체에서는 testService의 decrease()메서드를
호출하기 전,후로 트랜잭션을 수행해주게 된다.

문제는 testService의 decrease 메서드가 모두 동작되고 트랜잭션이 커밋되기까지 잠깐의 텀이 발생하게 된다는 것이다.

때문에 decrase 메서드가 완료되고 DB에 업데이트 쿼리가 전송되기 직전에 다른 스레드가 decrease 메서드에 접근이 가능하며, 다른 스레드는 갱신되기 이전값을
가져가기 때문에 이전과 동일한 동시성 문제가 발생하게 된다.

## Synchronized의 문제점

지금까지 살펴본 문제점은 Transactional 어노테이션을 주석처리하면 해결된다. 하지만 Synchronized 키워드 자체가 가지는 근본적인 한계점이 존재한다.

Synchronized는 하나의 프로세스 안에서만 보장이된다. 서버가 한대일 때는 DB에 접근을 서버 한대만 하기때문에 문제가 없지만, 서버가 여러대일 경우에는
여러 서버에서 DB에 접근하게 된다.

즉, Synchronized를 사용하면 동일 서버내 프로세스간에는 동시성이 보장되지만 서버와 서버간에는 동시성이 보장되지 않는다.
