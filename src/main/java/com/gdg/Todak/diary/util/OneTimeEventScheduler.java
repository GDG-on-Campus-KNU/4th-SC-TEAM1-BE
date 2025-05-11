package com.gdg.Todak.diary.util;

import com.gdg.Todak.diary.config.SchedulerConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class OneTimeEventScheduler {

    private final SchedulerConfig schedulerConfig;
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        this.taskScheduler = schedulerConfig.taskScheduler();
    }

    public void schedule(Runnable task, LocalDateTime targetTime) {
        Instant instant = targetTime.atZone(ZoneId.systemDefault()).toInstant();
        taskScheduler.schedule(task, Date.from(instant));
    }
}
