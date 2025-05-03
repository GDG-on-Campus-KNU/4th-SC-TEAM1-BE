package com.gdg.Todak.tree.domain;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.exception.BadRequestException;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PROTECTED)
@Getter
public class Tree {
    private long id;
    private int level;
    private int experience;
    private boolean isMaxGrowth;
    private Member member;

    public static Tree create(long id, int level, int experience, boolean isMaxGrowth, Member member) {
        return Tree.builder()
                .id(id)
                .level(level)
                .experience(experience)
                .isMaxGrowth(isMaxGrowth)
                .member(member)
                .build();
    }

    public void earnExperience(GrowthButton growthButton) {
        int experienceToEarn = convertExperienceToEarnPointByGrowthButton(growthButton);
        int experienceAfterEarned = this.experience + experienceToEarn;

        while (!isMaxGrowth) {
            int maxExperience = convertMaxExperienceByLevel(this.level);

            if (this.level == TreeConfig.MAX_LEVEL.getValue()) {
                if (experienceAfterEarned >= maxExperience) {
                    this.experience = maxExperience;
                    isMaxGrowth = true;
                } else {
                    this.experience = experienceAfterEarned;
                }
                return;
            }

            if (experienceAfterEarned >= maxExperience) {
                experienceAfterEarned -= maxExperience;
                levelUp();
            } else {
                this.experience = experienceAfterEarned;
                return;
            }
        }
    }

    private void levelUp() {
        this.level += 1;
    }

    private int convertExperienceToEarnPointByGrowthButton(GrowthButton growthButton) {
        switch (growthButton) {
            case GrowthButton.WATER -> {
                return TreeConfig.WATER_PLUS_EXPERIENCE.getValue();
            }
            case GrowthButton.SUN -> {
                return TreeConfig.SUN_PLUS_EXPERIENCE.getValue();
            }
            case GrowthButton.NUTRIENT -> {
                return TreeConfig.NUTRIENT_PLUS_EXPERIENCE.getValue();
            }
            default -> throw new BadRequestException("올바른 growthButton이 아닙니다.");
        }
    }

    private int convertMaxExperienceByLevel(int level) {
        switch (level) {
            case 1 -> {
                return TreeConfig.LEVEL_ONE_MAX_EXPERIENCE.getValue();
            }
            case 2 -> {
                return TreeConfig.LEVEL_TWO_MAX_EXPERIENCE.getValue();
            }
            case 3 -> {
                return TreeConfig.LEVEL_THREE_MAX_EXPERIENCE.getValue();
            }
            case 4 -> {
                return TreeConfig.LEVEL_FOUR_MAX_EXPERIENCE.getValue();
            }
            case 5 -> {
                return TreeConfig.LEVEL_FIVE_MAX_EXPERIENCE.getValue();
            }
            default -> throw new BadRequestException("올바른 level이 아닙니다.");
        }
    }

    public TreeEntityUpdateRequest toTreeEntityUpdateRequest() {
        return TreeEntityUpdateRequest.create(level, experience, isMaxGrowth);
    }

    public TreeInfoResponse toTreeInfoResponse() {
        return TreeInfoResponse.create(level, experience);
    }
}
