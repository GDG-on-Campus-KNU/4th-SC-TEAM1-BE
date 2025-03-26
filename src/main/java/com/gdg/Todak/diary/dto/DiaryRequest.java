package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;

public record DiaryRequest(
        String content,
        Emotion emotion,
        String storageUUID
) {
}
