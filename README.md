# Spring Scheduler 예제 프로젝트

## 개요

이 프로젝트는 Spring Boot에서 스케줄링 기능을 구현하고 ShedLock을 사용하여 분산 환경에서의 스케줄러 중복 실행을 방지하는 예제입니다.

## 주요 기능

-   **Spring Scheduling**: `@Scheduled` 어노테이션을 사용한 정기적인 작업 실행
-   **ShedLock 통합**: 분산 환경에서 스케줄러 중복 실행 방지
-   **MySQL 연동**: 스케줄러 락 정보를 MySQL 데이터베이스에 저장
-   **비동기 처리**: `@Async` 어노테이션을 사용한 비동기 스케줄러 실행

## 기술 스택

-   **Java**: 25
-   **Spring Boot**: 4.1.1 (Spring Framework 7)
-   **Gradle**: 9.8.1 (Wrapper 포함)
-   **ShedLock**: 7.10.1 (`shedlock-spring`, `shedlock-provider-jdbc-template`)
-   **MySQL**: 8.4 (Docker), `com.mysql:mysql-connector-j`
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

## 스케줄러 / 비동기 스레드 풀

-   **스케줄러 스레드 풀**: `SchedulerConfig`에서 `ThreadPoolTaskSchedulerCustomizer`로 스프링 부트가 생성하는 스케줄러를 커스터마이징합니다. (풀 크기 10, 스레드 이름 `jinhyunglee-N`)
-   **비동기 실행기**: `@Async` 메서드는 스프링 부트 기본 실행기(`applicationTaskExecutor`, 스레드 이름 `task-N`)에서 실행됩니다.

> `ThreadPoolTaskScheduler`를 직접 빈으로 등록하면 해당 빈이 `Executor`로도 인식되어 부트의 `applicationTaskExecutor`가 생성되지 않고, `@Async` 작업까지 스케줄러 풀에서 실행됩니다. 그래서 커스터마이저 방식을 사용합니다.

## ShedLock 설정

`SchedulerConfiguration`에서 `@EnableSchedulerLock`으로 ShedLock을 활성화하고, `JdbcTemplateLockProvider`를 등록합니다. 락 시간은 서버 간 시계 차이의 영향을 받지 않도록 DB 시간(`usingDbTime()`)을 기준으로 계산합니다.

```java
@Configuration
@EnableSchedulerLock(defaultLockAtLeastFor = "PT5S", defaultLockAtMostFor = "PT10S")
public class SchedulerConfiguration {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()
                        .build()
        );
    }
}
```

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
│   │   │   ├── SchedulerConfig.java           # 스케줄러 스레드 풀 설정
│   │   │   └── SchedulerConfiguration.java    # ShedLock 설정 (LockProvider)
│   │   └── service/
│   │       ├── DelayScheduledComponent.java   # fixedDelay 스케줄러
│   │       └── RateScheduledComponent.java    # fixedRate 스케줄러
│   └── resources/
│       ├── application.yml                    # 애플리케이션 설정
│       ├── docker-compose.yml                 # MySQL 컨테이너 설정
│       └── init.sql                          # 데이터베이스 초기화 스크립트
```

## 실행 방법

### 0. 사전 준비

-   **JDK 25** 설치 및 `JAVA_HOME` 설정 (Gradle 9.8.1은 JDK 17 이상에서 실행되며, 빌드는 Java 25 툴체인을 사용합니다)
-   **Docker** (Docker Compose v2)

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\java\jdk-25.0.4.1"

# macOS / Linux / Git Bash
export JAVA_HOME=/path/to/jdk-25
```

### 1. 데이터베이스 준비

```bash
# 프로젝트 루트에서 MySQL 컨테이너 실행 (init.sql 로 DB, 사용자, shedlock 테이블 생성)
docker compose -f src/main/resources/docker-compose.yml up -d mysql
```

### 2. 애플리케이션 실행

```bash
# Gradle을 사용한 실행
./gradlew bootRun

# 빌드 후 jar 실행
./gradlew build
java -jar build/libs/spring-scheduler-0.0.1-SNAPSHOT.jar

# 또는 IDE에서 SpringSchedulerApplication.java 실행
```

### 3. 로그 확인

애플리케이션이 시작되면 다음과 같은 로그를 확인할 수 있습니다:

```
:: Spring Boot ::                (v4.1.1)
Starting SpringSchedulerApplication using Java 25.0.4.1 ...
[  jinhyunglee-1] c.e.s.service.RateScheduledComponent  : [BEG] fixedRateJobSync.
[  jinhyunglee-3] c.e.s.service.DelayScheduledComponent : [BEG] fixedDelayJob.
[         task-1] c.e.s.service.RateScheduledComponent  : [BEG] fixedRateJobAsync. - 0
[  jinhyunglee-1] c.e.s.service.RateScheduledComponent  : [END] fixedRateJobSync.
[  jinhyunglee-3] c.e.s.service.DelayScheduledComponent : [END] fixedDelayJob.
[         task-1] c.e.s.service.RateScheduledComponent  : [END] fixedRateJobAsync. - 0
```

락 정보는 다음과 같이 확인할 수 있습니다.

```bash
docker compose -f src/main/resources/docker-compose.yml exec mysql mysql -uappuser -p123456 appdb -e "SELECT * FROM shedlock"
```

## 데이터베이스 설정

### MySQL 연결 정보

-   **URL**: `jdbc:mysql://localhost:3306/appdb?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC`
-   **사용자명**: `appuser`
-   **비밀번호**: `123456`
-   **포트**: `3306`

> MySQL 8.x 의 기본 인증 방식(`caching_sha2_password`)에서 `useSSL=false`로 접속하려면 `allowPublicKeyRetrieval=true` 옵션이 필요합니다.

### ShedLock 테이블

ShedLock은 테이블을 자동으로 생성하지 않습니다. 락 정보를 저장할 `shedlock` 테이블은 컨테이너 최초 기동 시 `init.sql`에서 생성됩니다.

```sql
CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

## 주의사항

1. **분산 환경**: 여러 인스턴스에서 동일한 스케줄러가 실행될 때 ShedLock이 중복 실행을 방지합니다.

2. **락 시간 설정**:

    - `lockAtLeastFor`는 너무 짧게 설정하면 중복 실행 위험
    - `lockAtMostFor`는 너무 길게 설정하면 장애 시 복구 지연

3. **비동기 처리**: `@Async`를 사용할 때는 스레드 풀 설정을 고려해야 합니다.

## 참고 자료

-   [Spring Scheduling Documentation](https://spring.io/guides/gs/scheduling-tasks/)
-   [ShedLock Documentation](https://github.com/lukas-krecan/ShedLock)
-   [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/reference/)
-   [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
