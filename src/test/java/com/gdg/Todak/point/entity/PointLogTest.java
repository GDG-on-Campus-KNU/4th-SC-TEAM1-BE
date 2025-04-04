package com.gdg.Todak.point.entity;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointLogTest {

    private Member member;
    private PointLog pointLog;

    @BeforeEach
    void setUp() {
        member = new Member("user1", "email1", "pw", "010-1234-5678", "nickname");

        pointLog = PointLog.builder()
                .member(member)
                .point(100)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();
    }

    @DisplayName("PointLog 객체가 정상적으로 생성되어야 한다")
    @Test
    void constructorTest() {
        assertThat(pointLog).isNotNull();
        assertThat(pointLog.getMember()).isEqualTo(member);
        assertThat(pointLog.getPoint()).isEqualTo(100);
        assertThat(pointLog.getPointType()).isEqualTo(PointType.DIARY);
        assertThat(pointLog.getPointStatus()).isEqualTo(PointStatus.EARNED);
    }
}
