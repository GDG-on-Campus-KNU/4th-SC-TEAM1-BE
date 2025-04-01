package com.gdg.Todak.diary.dto;

import com.gdg.Todak.member.domain.Member;

public record AICommentRequest(
        Member member,
        String content
) {
}
