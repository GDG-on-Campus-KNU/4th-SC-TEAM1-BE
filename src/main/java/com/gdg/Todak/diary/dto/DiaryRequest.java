package com.gdg.Todak.diary.dto;

import com.gdg.Todak.diary.Emotion;
import jakarta.validation.constraints.NotEmpty;

public record DiaryRequest(
        String content,
        Emotion emotion,
        @NotEmpty
        String storageUUID
) {
}
