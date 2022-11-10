package com.example.springscheduler.service;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableSchedulerLock(defaultLockAtLeastFor = "PT5S", defaultLockAtMostFor = "PT10S")
public class RateScheduledComponent {

    // 해당 작업의 시작 시점부터 시간을 카운트
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    @SchedulerLock(name = "fixedRateJobSync", lockAtMostFor = "PT1S", lockAtLeastFor = "PT1S")
    public void fixedRateJobSync() throws InterruptedException {
        log.info("[BEG] fixedRateJobSync.");
        Thread.sleep(1000);
        log.info("[END] fixedRateJobSync.");
    }

    private static int i = 0;

    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    @SchedulerLock(name = "fixedRateJobAsync", lockAtMostFor = "PT1S", lockAtLeastFor = "PT1S")
    @Async
    public void fixedRateJobAsync() throws InterruptedException {
        int j = i;
        i++;
        log.info("[BEG] fixedRateJobAsync. - {}", j);
        Thread.sleep(2000);
        log.info("[END] fixedRateJobAsync. - {}", j);
    }

}
