package com.example.springscheduler.config;

import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {
    private static final int POOL_SIZE = 10;

    // 스케줄러 생성/종료는 스프링 부트에 맡기고 설정값만 커스터마이징
    // (ThreadPoolTaskScheduler 를 직접 빈으로 등록하면 @Async 기본 실행기(applicationTaskExecutor)가 비활성화됨)
    @Bean
    public ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizer() {
        return threadPoolTaskScheduler -> {
            threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
            threadPoolTaskScheduler.setThreadNamePrefix("jinhyunglee-");
        };
    }
}
