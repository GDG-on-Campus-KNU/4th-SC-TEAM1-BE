package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

public record DiaryResponse(
        Long diaryId,
        String title,
        String content,
        Emotion emotion
) {
}
