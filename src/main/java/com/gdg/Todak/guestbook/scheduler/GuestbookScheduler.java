package com.gdg.Todak.guestbook.scheduler;

import com.gdg.Todak.guestbook.service.GuestbookService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class GuestbookScheduler {

    private final GuestbookService guestbookService;

    @SchedulerLock(name = "guestbook_delete_cron_lock", lockAtLeastFor = "30s", lockAtMostFor = "5m")
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredGuestbooks() {
        Instant now = Instant.now();
        guestbookService.deleteExpiredGuestbooks(now);
    }
}
