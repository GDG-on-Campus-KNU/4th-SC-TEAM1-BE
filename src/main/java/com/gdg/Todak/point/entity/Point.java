package com.gdg.Todak.point.entity;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.exception.BadRequestException;
import com.gdg.Todak.tree.domain.GrowthButton;
import com.gdg.Todak.tree.domain.TreeExperiencePolicy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter
public class Point {

    private final static int INITIAL_POINT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @JoinColumn(name = "member_id")
    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;
    @NotNull
    @Builder.Default
    private int point = INITIAL_POINT;

    public void earnPoint(int point) {
        this.point += point;
    }

    public void consumePoint(int point) {
        if (this.point - point < 0) {
            throw new BadRequestException(
                    String.format("요청하신 포인트(%dP)를 사용할 수 없습니다. 현재 보유 포인트는 %dP입니다.", point, this.point)
            );
        }
        this.point -= point;
    }

    public int consumePointByGrowthButton(GrowthButton growthButton) {
        int pointToSpend = convertSpendPointByGrowthButton(growthButton);
        consumePoint(pointToSpend);
        return pointToSpend;
    }

    public int consumePointToGetCommentWriterId(int pointToSpend) {
        consumePoint(pointToSpend);
        return pointToSpend;
    }

    public PointType convertPointTypeByGrowthButton(GrowthButton growthButton) {
        switch (growthButton) {
            case GrowthButton.WATER -> {
                return PointType.GROWTH_WATER;
            }
            case GrowthButton.SUN -> {
                return PointType.GROWTH_SUN;
            }
            case GrowthButton.NUTRIENT -> {
                return PointType.GROWTH_NUTRIENT;
            }
            default -> throw new BadRequestException("올바른 growthButton이 아닙니다.");
        }
    }

    private int convertSpendPointByGrowthButton(GrowthButton growthButton) {
        switch (growthButton) {
            case GrowthButton.WATER -> {
                return TreeExperiencePolicy.WATER_SPEND.getValue();
            }
            case GrowthButton.SUN -> {
                return TreeExperiencePolicy.SUN_SPEND.getValue();
            }
            case GrowthButton.NUTRIENT -> {
                return TreeExperiencePolicy.NUTRIENT_SPEND.getValue();
            }
            default -> throw new BadRequestException("올바른 growthButton이 아닙니다.");
        }
    }
}
