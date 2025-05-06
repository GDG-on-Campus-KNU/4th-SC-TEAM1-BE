package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.util.OneTimeEventScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class SchedulerService {

    public static final int MAX_MINUTE = 20;

    private final OneTimeEventScheduler oneTimeEventScheduler;
    private final CommentService commentService;

    public void scheduleSavingCommentByAI(Diary diary) {
        LocalDateTime targetLocalDateTime = createRandomLocalDateTime();
        oneTimeEventScheduler.schedule(() -> {
            commentService.saveCommentByAI(diary);
        }, targetLocalDateTime);
    }

    private static LocalDateTime createRandomLocalDateTime() {
        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        return LocalDateTime.now().plusMinutes(rand.nextInt(MAX_MINUTE) + 1);
    }
}
