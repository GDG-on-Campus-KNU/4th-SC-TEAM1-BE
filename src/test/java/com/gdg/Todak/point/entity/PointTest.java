package com.gdg.Todak.point.entity;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointTest {

    private Member member;
    private Point point;

    @BeforeEach
    void setUp() {
        member = new Member("user1", "email1", "pw", "010-1234-5678", "nickname");
        point = Point.builder()
                .member(member)
                .build();
    }

    @DisplayName("Point 객체가 정상적으로 생성되어야 한다")
    @Test
    void constructorTest() {
        assertThat(point).isNotNull();
        assertThat(point.getPoint()).isEqualTo(0);
        assertThat(point.getMember()).isEqualTo(member);
    }

    @DisplayName("포인트를 적립하면 현재 포인트에 누적되어야 한다")
    @Test
    void earnPointTest() {
        // when
        point.earnPoint(100);

        // then
        assertThat(point.getPoint()).isEqualTo(100);
    }

    @DisplayName("포인트를 사용할 경우 차감되어야 한다")
    @Test
    void consumePointTest() {
        // given
        point.earnPoint(150);

        // when
        point.consumePoint(50);

        // then
        assertThat(point.getPoint()).isEqualTo(100);
    }

    @DisplayName("보유 포인트보다 많은 포인트를 사용하면 예외가 발생해야 한다")
    @Test
    void consumePoint_insufficientTest() {
        // given
        point.earnPoint(30);

        // when & then
        assertThatThrownBy(() -> point.consumePoint(50))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("남은 포인트가 0 미만일 수 없습니다.");
    }
}
