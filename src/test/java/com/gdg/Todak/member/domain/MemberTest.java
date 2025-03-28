package com.gdg.Todak.member.domain;

import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("중복된 유저 아이디로 생성할 수 없다.")
    @Test
    void duplicateUserIdTest() {
        // given
        String userId = "userId";

        Member member1 = Member.of(userId, "password", "nickname", "imageUrl", "salt");
        Member member2 = Member.of(userId, "password", "nickname", "imageUrl", "salt");

        // when
        memberRepository.save(member1);

        // then
        assertThatThrownBy(() -> memberRepository.save(member2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}