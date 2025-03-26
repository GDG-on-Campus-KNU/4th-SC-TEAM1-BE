package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

public record DiaryUpdateRequest(
        String content,
        Emotion emotion
) {
}
