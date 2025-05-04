package com.gdg.Todak.tree.domain;

import lombok.Getter;

@Getter
public enum TreeExperiencePolicy {
    INITIAL_LEVEL(1),
    INITIAL_EXPERIENCE(0),
    LEVEL_ONE_MAX_EXPERIENCE(100),
    LEVEL_TWO_MAX_EXPERIENCE(150),
    LEVEL_THREE_MAX_EXPERIENCE(200),
    LEVEL_FOUR_MAX_EXPERIENCE(250),
    LEVEL_FIVE_MAX_EXPERIENCE(300),
    MAX_LEVEL(5),
    WATER_PLUS_EXPERIENCE(10),
    SUN_PLUS_EXPERIENCE(20),
    NUTRIENT_PLUS_EXPERIENCE(30),
    WATER_SPEND(10),
    SUN_SPEND(15),
    NUTRIENT_SPEND(20);

    private final int value;

    TreeExperiencePolicy(int value) {
        this.value = value;
    }
}
