package com.gdg.Todak.tree.domain;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TreeTest {

    private Member member;
    private Tree tree;

    @BeforeEach
    void setUp() {
        member = new Member("user1", "email1", "pw", "010-1234-5678", "nickname");
        tree = Tree.create(1L,
                TreeExperiencePolicy.INITIAL_LEVEL.getValue(),
                TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue(),
                false,
                member);
    }

    @DisplayName("Tree 객체가 정상적으로 생성되어야 한다")
    @Test
    void createTreeTest() {
        // then
        assertThat(tree).isNotNull();
        assertThat(tree.getId()).isEqualTo(1L);
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue());
        assertThat(tree.isMaxGrowth()).isFalse();
        assertThat(tree.getMember()).isEqualTo(member);
    }

    @DisplayName("물 성장 버튼으로 경험치가 증가해야 한다")
    @Test
    void earnExperienceWithWaterTest() {
        // when
        tree.earnExperience(GrowthButton.WATER);

        // then
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.WATER_PLUS_EXPERIENCE.getValue());
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(tree.isMaxGrowth()).isFalse();
    }

    @DisplayName("햇빛 성장 버튼으로 경험치가 증가해야 한다")
    @Test
    void earnExperienceWithSunTest() {
        // when
        tree.earnExperience(GrowthButton.SUN);

        // then
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.SUN_PLUS_EXPERIENCE.getValue());
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(tree.isMaxGrowth()).isFalse();
    }

    @DisplayName("영양분 성장 버튼으로 경험치가 증가해야 한다")
    @Test
    void earnExperienceWithNutrientTest() {
        // when
        tree.earnExperience(GrowthButton.NUTRIENT);

        // then
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue());
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(tree.isMaxGrowth()).isFalse();
    }

    @DisplayName("경험치가 최대치를 넘으면 레벨업이 되어야 한다")
    @Test
    void levelUpWhenExperienceReachesMaxTest() {
        // given
        int numOfActions = TreeExperiencePolicy.LEVEL_ONE_MAX_EXPERIENCE.getValue() /
                TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue();

        if (TreeExperiencePolicy.LEVEL_ONE_MAX_EXPERIENCE.getValue() %
                TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue() != 0) {
            numOfActions++;
        }

        // when
        for (int i = 0; i < numOfActions; i++) {
            tree.earnExperience(GrowthButton.NUTRIENT);
        }

        // then
        assertThat(tree.getLevel()).isEqualTo(2);
        assertThat(tree.getExperience()).isLessThan(TreeExperiencePolicy.LEVEL_TWO_MAX_EXPERIENCE.getValue());
    }

    @DisplayName("여러번 레벨업을 할 수 있어야 한다")
    @Test
    void multipleConsecutiveLevelUpsTest() {
        // given
        int totalExperienceNeeded = TreeExperiencePolicy.LEVEL_ONE_MAX_EXPERIENCE.getValue() +
                TreeExperiencePolicy.LEVEL_TWO_MAX_EXPERIENCE.getValue() + 50;

        int numOfActions = totalExperienceNeeded / TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue();

        if (totalExperienceNeeded % TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue() != 0) {
            numOfActions++;
        }

        // when
        for (int i = 0; i < numOfActions; i++) {
            tree.earnExperience(GrowthButton.NUTRIENT);
        }

        // then
        assertThat(tree.getLevel()).isEqualTo(3);
        assertThat(tree.getExperience()).isGreaterThan(0);
        assertThat(tree.getExperience()).isLessThan(TreeExperiencePolicy.LEVEL_THREE_MAX_EXPERIENCE.getValue());
    }

    @DisplayName("최대 레벨에 도달하면 isMaxGrowth가 true가 되어야 한다")
    @Test
    void maxGrowthWhenReachingMaxLevelTest() {
        // given
        int totalExperience = 0;
        for (int i = 1; i <= TreeExperiencePolicy.MAX_LEVEL.getValue(); i++) {
            switch (i) {
                case 1 -> totalExperience += TreeExperiencePolicy.LEVEL_ONE_MAX_EXPERIENCE.getValue();
                case 2 -> totalExperience += TreeExperiencePolicy.LEVEL_TWO_MAX_EXPERIENCE.getValue();
                case 3 -> totalExperience += TreeExperiencePolicy.LEVEL_THREE_MAX_EXPERIENCE.getValue();
                case 4 -> totalExperience += TreeExperiencePolicy.LEVEL_FOUR_MAX_EXPERIENCE.getValue();
                case 5 -> totalExperience += TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue();
                default -> totalExperience = 0;
            }
        }

        int numOfActions = totalExperience / TreeExperiencePolicy.NUTRIENT_PLUS_EXPERIENCE.getValue() + 1;

        // when
        for (int i = 0; i < numOfActions; i++) {
            tree.earnExperience(GrowthButton.NUTRIENT);
        }

        // then
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.MAX_LEVEL.getValue());
        assertThat(tree.isMaxGrowth()).isTrue();
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue());
    }

    @DisplayName("최대 성장에 도달한 후에는 경험치가 증가하지 않아야 한다")
    @Test
    void noMoreExperienceAfterMaxGrowthTest() {
        // given
        tree = Tree.create(1L, TreeExperiencePolicy.MAX_LEVEL.getValue(),
                TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue(), true, member);

        // when
        tree.earnExperience(GrowthButton.NUTRIENT);

        // then
        assertThat(tree.getLevel()).isEqualTo(TreeExperiencePolicy.MAX_LEVEL.getValue());
        assertThat(tree.getExperience()).isEqualTo(TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue());
        assertThat(tree.isMaxGrowth()).isTrue();
    }

    @DisplayName("DTO 변환 메서드 테스트 - toTreeEntityUpdateRequest")
    @Test
    void toTreeEntityUpdateRequestTest() {
        // when
        TreeEntityUpdateRequest request = tree.toTreeEntityUpdateRequest();

        // then
        assertThat(request.level()).isEqualTo(tree.getLevel());
        assertThat(request.experience()).isEqualTo(tree.getExperience());
        assertThat(request.isMaxGrowth()).isEqualTo(tree.isMaxGrowth());
    }

    @DisplayName("DTO 변환 메서드 테스트 - toTreeInfoResponse")
    @Test
    void toTreeInfoResponseTest() {
        // when
        TreeInfoResponse response = tree.toTreeInfoResponse();

        // then
        assertThat(response.level()).isEqualTo(tree.getLevel());
        assertThat(response.experience()).isEqualTo(tree.getExperience());
    }
}
