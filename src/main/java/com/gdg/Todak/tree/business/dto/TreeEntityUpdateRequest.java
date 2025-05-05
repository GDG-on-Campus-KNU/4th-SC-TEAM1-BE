package com.gdg.Todak.tree.business.dto;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TreeEntityUpdateRequest(
        int level,
        int experience,
        boolean isMaxGrowth
) {
    public static TreeEntityUpdateRequest create(int level, int experience, boolean isMaxGrowth) {
        return TreeEntityUpdateRequest.builder()
                .level(level)
                .experience(experience)
                .isMaxGrowth(isMaxGrowth)
                .build();
    }
}
