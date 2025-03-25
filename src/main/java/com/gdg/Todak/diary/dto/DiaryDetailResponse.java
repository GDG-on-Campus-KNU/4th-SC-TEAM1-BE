package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

import java.time.LocalDateTime;

public record DiaryDetailResponse(
        Long diaryId,
        LocalDateTime createdAt,
        String content,
        Emotion emotion,
        boolean isWriter
) {
}
