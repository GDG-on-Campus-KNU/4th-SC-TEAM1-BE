package com.gdg.Todak.tree.business.dto;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.domain.Tree;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TreeEntityDto(
        long id,
        int level,
        int experience,
        boolean isMaxGrowth,
        Member member
) {
    public static TreeEntityDto create(long id, int level, int experience, boolean isMaxGrowth, Member member) {
        return TreeEntityDto.builder()
                .id(id)
                .level(level)
                .experience(experience)
                .isMaxGrowth(isMaxGrowth)
                .member(member)
                .build();
    }

    public Tree toDomain() {
        return Tree.create(id, level, experience, isMaxGrowth, member);
    }
}
