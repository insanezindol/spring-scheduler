package com.example.springscheduler.service;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableSchedulerLock(defaultLockAtLeastFor = "PT5S", defaultLockAtMostFor = "PT10S")
public class DelayScheduledComponent {

    // 해당 작업이 끝난 시점부터 시간을 카운트
    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    @SchedulerLock(name = "fixedDelayJob", lockAtMostFor = "PT1S", lockAtLeastFor = "PT1S")
    public void fixedDelayJob() throws InterruptedException {
        log.info("[BEG] fixedDelayJob.");
        Thread.sleep(1000);
        log.info("[END] fixedDelayJob.");
    }

    // SchedulerLock
    // name은 스케줄러 락을 구분하는 고유한 구분자

    // lockAtLeastFor - 메소드간의 시간 차이 정함. PT5M 은 최소 5분간 이 락을 홀드하겠다는 의미임. 잠금을 유지해야 하는 기간을 지정하는 속성
    // -> 해당 스케줄러가 시작되고 난 후 최소한 기다려주는 시간

    // lockAtMostFor - 실행 노드가 죽었을 때 이 락이 얼마나 길게  유지되어야 하는지를 특정함, 잠금을 유지해야 하는 최소 시간을 지정하는 속성
    // -> 모종의 이유로 스케줄러에서 에러가 나거나 시간이 오래걸릴때 다른 스케줄러가 기다려주는 시간


}
