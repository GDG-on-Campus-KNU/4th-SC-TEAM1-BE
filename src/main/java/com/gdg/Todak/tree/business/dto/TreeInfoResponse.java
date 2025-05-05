package com.gdg.Todak.tree.business.dto;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TreeInfoResponse(
        int level,
        int experience
) {
    public static TreeInfoResponse create(int level, int experience) {
        return TreeInfoResponse.builder()
                .level(level)
                .experience(experience)
                .build();
    }
}
