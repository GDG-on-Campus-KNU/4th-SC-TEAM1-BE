package com.gdg.Todak.point.entity;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.exception.BadRequestException;
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
            throw new BadRequestException("남은 포인트가 0 미만일 수 없습니다.");
        }
        this.point -= point;
    }
}
