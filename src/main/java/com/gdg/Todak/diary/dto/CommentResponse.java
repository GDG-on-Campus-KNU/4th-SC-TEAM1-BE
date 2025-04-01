package com.gdg.Todak.diary.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String writerName,
        String content,
        boolean isWriter
) {
}
