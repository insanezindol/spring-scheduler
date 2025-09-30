# Spring Scheduler 예제 프로젝트

## 개요

이 프로젝트는 Spring Boot에서 스케줄링 기능을 구현하고 ShedLock을 사용하여 분산 환경에서의 스케줄러 중복 실행을 방지하는 예제입니다.

## 주요 기능

-   **Spring Scheduling**: `@Scheduled` 어노테이션을 사용한 정기적인 작업 실행
-   **ShedLock 통합**: 분산 환경에서 스케줄러 중복 실행 방지
-   **MySQL 연동**: 스케줄러 락 정보를 MySQL 데이터베이스에 저장
-   **비동기 처리**: `@Async` 어노테이션을 사용한 비동기 스케줄러 실행

## 기술 스택

-   **Java**: 11
-   **Spring Boot**: 2.7.5
-   **ShedLock**: 4.42.0
-   **MySQL**: 5.7
-   **Lombok**: 코드 간소화

## 스케줄러 유형

### 1. DelayScheduledComponent (fixedDelay)

-   **실행 방식**: 작업이 완료된 시점부터 다음 실행까지의 대기 시간 설정
-   **특징**: 이전 작업의 완료를 보장하고 지정된 시간 후 다음 작업 실행
-   **설정**: 초기 지연 10초, 작업 완료 후 5초 대기

```java
@Scheduled(initialDelay = 10000, fixedDelay = 5000)
```

### 2. RateScheduledComponent (fixedRate)

-   **실행 방식**: 작업 시작 시점부터 다음 실행까지의 간격 설정
-   **특징**:
    -   동기 실행: 작업이 길어져도 다음 실행이 대기
    -   비동기 실행: 작업이 병렬로 실행 가능
-   **설정**: 초기 지연 10초, 5초 간격으로 실행

```java
@Scheduled(initialDelay = 10000, fixedRate = 5000)
```

## ShedLock 설정

### 주요 속성

-   **name**: 스케줄러 락의 고유 식별자
-   **lockAtLeastFor**: 락을 최소한 유지해야 하는 시간 (중복 실행 방지)
-   **lockAtMostFor**: 락을 최대한 유지할 수 있는 시간 (데드락 방지)

### 예제

```java
@SchedulerLock(
    name = "fixedDelayJob",
    lockAtMostFor = "PT1S",
    lockAtLeastFor = "PT1S"
)
```

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/springscheduler/
│   │   ├── SpringSchedulerApplication.java     # 메인 애플리케이션 클래스
│   │   ├── config/
│   │   │   ├── SchedulerConfig.java           # 스케줄러 설정
│   │   │   └── SchedulerConfiguration.java    # 추가 스케줄러 설정
│   │   └── service/
│   │       ├── DelayScheduledComponent.java   # fixedDelay 스케줄러
│   │       └── RateScheduledComponent.java    # fixedRate 스케줄러
│   └── resources/
│       ├── application.yml                    # 애플리케이션 설정
│       ├── docker-compose.yml                 # MySQL 컨테이너 설정
│       └── init.sql                          # 데이터베이스 초기화 스크립트
```

## 실행 방법

### 1. 데이터베이스 준비

```bash
# MySQL 컨테이너 실행
docker-compose up -d mysql
```

### 2. 애플리케이션 실행

```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 또는 IDE에서 SpringSchedulerApplication.java 실행
```

### 3. 로그 확인

애플리케이션이 시작되면 다음과 같은 로그를 확인할 수 있습니다:

```
[BEG] fixedDelayJob.
[END] fixedDelayJob.
[BEG] fixedRateJobSync.
[END] fixedRateJobSync.
[BEG] fixedRateJobAsync. - 0
[END] fixedRateJobAsync. - 0
```

## 데이터베이스 설정

### MySQL 연결 정보

-   **URL**: `jdbc:mysql://localhost:3306/appdb`
-   **사용자명**: `appuser`
-   **비밀번호**: `123456`
-   **포트**: `3306`

### ShedLock 테이블

ShedLock은 MySQL 데이터베이스에 락 정보를 저장하기 위한 테이블을 자동으로 생성합니다.

## 주의사항

1. **분산 환경**: 여러 인스턴스에서 동일한 스케줄러가 실행될 때 ShedLock이 중복 실행을 방지합니다.

2. **락 시간 설정**:

    - `lockAtLeastFor`는 너무 짧게 설정하면 중복 실행 위험
    - `lockAtMostFor`는 너무 길게 설정하면 장애 시 복구 지연

3. **비동기 처리**: `@Async`를 사용할 때는 스레드 풀 설정을 고려해야 합니다.

## 참고 자료

-   [Spring Scheduling Documentation](https://spring.io/guides/gs/scheduling-tasks/)
-   [ShedLock Documentation](https://github.com/lukas-krecan/ShedLock)
-   [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
