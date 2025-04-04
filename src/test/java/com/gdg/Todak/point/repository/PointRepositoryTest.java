package com.gdg.Todak.point.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.entity.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PointRepositoryTest {

    @Autowired
    PointRepository pointRepository;

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("Point 객체가 정상적으로 저장되어야 한다")
    @Test
    void pointSaveTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "email1", "pw", "010-1234-5678", "nickname"));
        Point point = Point.builder()
                .member(member)
                .point(100)
                .build();

        // when
        Point savedPoint = pointRepository.save(point);

        // then
        assertThat(savedPoint.getId()).isNotNull();
        assertThat(savedPoint.getPoint()).isEqualTo(100);
        assertThat(savedPoint.getMember().getId()).isEqualTo(member.getId());
    }

    @DisplayName("existsByMember() 테스트 - 포인트 객체 존재 여부")
    @Test
    void existsByMemberTest() {
        // given
        Member member = memberRepository.save(new Member("user2", "email2", "pw", "010-1234-5678", "nickname"));
        Point point = Point.builder()
                .member(member)
                .point(50)
                .build();
        pointRepository.save(point);

        // when
        boolean exists = pointRepository.existsByMember(member);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("findByMember() 테스트 - 멤버로 포인트 조회")
    @Test
    void findByMemberTest() {
        // given
        Member member = memberRepository.save(new Member("user3", "email3", "pw", "010-1234-5678", "nickname"));
        Point point = Point.builder()
                .member(member)
                .point(200)
                .build();
        pointRepository.save(point);

        // when
        Optional<Point> result = pointRepository.findByMember(member);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPoint()).isEqualTo(200);
    }

    @DisplayName("findByMember() 테스트 - 존재하지 않는 멤버로 조회 시 Optional.empty()")
    @Test
    void findByMember_notFoundTest() {
        // given
        Member member = memberRepository.save(new Member("user4", "email4", "pw", "010-0000-0000", "nickname"));

        // when
        Optional<Point> result = pointRepository.findByMember(member);

        // then
        assertThat(result).isEmpty();
    }
}
