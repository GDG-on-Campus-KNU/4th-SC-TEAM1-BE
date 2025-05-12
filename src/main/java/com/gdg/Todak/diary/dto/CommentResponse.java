package com.gdg.Todak.diary.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String nickname,
        String userId,
        String content,
        boolean isWriter,
        boolean isAnonymous
) {
    public static CommentResponse of(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                                     String nickname, String userId, String content, boolean isWriter, boolean isAnonymous) {
        return new CommentResponse(id, createdAt, updatedAt, nickname, userId, content, isWriter, isAnonymous);
    }
}
