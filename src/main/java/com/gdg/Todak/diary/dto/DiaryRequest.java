package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

public record DiaryRequest(
    String title,
    String content,
    Emotion emotion
) {
}
