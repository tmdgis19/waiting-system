## Web Flux
### Reactive Streams
> Reactive Streams is an initiative to provide a standard 
> for asynchronous stream processing with non-blocking back pressure.

Reactive Streams의 공식 사이트에 있는 문장이다. 이보다 명확하게 설명하기는 어렵기 때문에 인용했다.
해석하자면 Reactive Streams는 비차단 백프레셔(역압)를 처리하기 위 비동기 스트림 표준이란 뜻이다.

영어를 직역하자니 조금 어색한데, 핵심 키워드는 Non-Blocking, Back Pressure, 비동기 스트림이라고 생각한다.
그럼 하나하나 살펴보겠다.

#### Non-Blocking
Non-Blocking이란 말 그대로 막히지 않는다는 의미이다. 그럼 무엇이 막히지 않는다는 것인지를 알아야한다. 
일반적으로 I/O에 대해 말할 때 Blocking, Non-Blocking을 말한다. 

예를 들어 파일을 저장하는 작업을 수행하는 프로그램이 있다고 해보자. 프로그램이 실행되면 메모리에 올라가 프로세스가 되고 
프로세스의 실행 단위는 스레드이므로 결과적으로 스레드가 파일 저장 작업을 수행하는 것이다. 스레드가 파일 저장 작업을 하는 절차는 간략히 다음과 같다.

1. 스레드가 파일 쓰기 작업을 위해 운영체제에 system call을 날린다.
2. 이 때 스레드의 상태는 대기 상태로 변경된다.
3. 운영체제는 파일 시스템을 통해 쓰기 작업을 수행한다. 
   1. 이 때 디스크에 바로 쓰는 게 아니라 메모리의 페이지 캐시 또는 버퍼 캐시에 데이터를 1차적으로 저장한다.
   2. 왜냐하면 디스크는 상대적으로 메모리보다 속도가 느리기 때문.
   3. 운영체제는 I/O 스케줄링을 통해 메모리에 있는 데이터를 Disk로 저장한다.
   4. 결과적으로 쓰기 작업을 지연시켜 용량이 큰 파일도 보다 빠르게 저장할 수 있게 된다.
4. 쓰기 작업이 끝나면 운영체제는 스레드에 완료 알림을 보내고 스레드는 대기 상태에서 깨어나 다음 작업을 수행한다.

이처럼 파일 쓰기 작업을 할 때 스레드는 작업이 끝날 때까지 대기해야 한다. 즉 다른 코드가 실행되지 못하고 막힌다는 의미이다.
그럼 Non-Blocking은 I/O 작업이 발생해도 스레드의 작업이 막히지 않고 수행된다는 개념으로 유추해 볼 수 있다.

위 예시로 들면 system call을 호출하는 즉시 바로 return을 한다는 의미이다. 스레드는 호출 함수가 종료 되었으므로 이후 작업을 수행할 수 있게된다.
함수 종료와 무관하게 운영체제는 파일 쓰기 작업을 수행하게 되고 작업이 끝나면 똑같이 완료 알림을 보낸다. 
그런데 이 때 중요한 게 system call을 호출한 스레드와 완료 알림을 받는 스레드가 다를 수 있다는 것이다. 

항상 헷갈렸던 게 Non-Blocking과 함께 나오는 비동기라는 말의 의미였다. 흔히 '동기'라고 할 때 '일치하다'라는 의미로 쓰이게 되는데 
비동기 Non-Blocking이라고 했을 때 무엇이 일치하지 않는다는 것인지 잘 알지 못했다. 이번에 Non-Blocking을 살펴보며 I/O 작업을 호출한 스레드와
작업 결과를 반환 받는 스레드가 일치하지 않을 수 있다는 것으로 그 의미를 잘 이해할 수 있었다.

#### Back Pressure
Non-Blocking에 대해 간략히 알아봤는데, 그럼 Reactive Streams에서 Non-Blocking은 어떻게 적용되는가?
바로 Back Pressure라는 방법을 통해 구현된다. Reactive Streams는 이벤트 기반으로 작동한다.
즉 HTTP 요청과 같은 이벤트가 발생하면 Publisher가 이벤트를 발행하고 Subscriber가 이벤트를 구독하여 처리한다.

그런데 Subscriber의 이벤트 처리가 느려서 Publisher가 발행한 이벤트가 처리되지 못하고 쌓이기만 하면 어떻게 될까? 
이벤트 대기열을 만들어 일정량은 보관할 수 있겠지만 쌓이는 속도가 빠르면 결국 대기열이 가득찰 것이다. 그렇다고 대기열을 제한 없이 무작정 사이즈를 늘리면
메모리가 부족해질 것이다. 이를 극복하기 위해 Back Pressure라는 개념이 등장했다. Back Pressure는 Publisher가 이벤트를 일방적으로 발행하는 것이 아니라
Subscriber가 처리 가능한 수만큼 요청하면 그 수만큼만 이벤트를 발행하는 방식을 말한다. 처리 가능한 수만큼 이벤트를 발행하니 이벤트가 쌓여서 발생하는 문제는 해결된다.

## Redis 

## 주요 API 개요

## 참고 자료
https://engineering.linecorp.com/ko/blog/reactive-streams-with-armeria-1