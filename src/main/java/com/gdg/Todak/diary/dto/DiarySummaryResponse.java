package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

import java.time.LocalDate;

public record DiarySummaryResponse(
        Long diaryId,
        LocalDate createdAt,
        Emotion emotion
) {
}
