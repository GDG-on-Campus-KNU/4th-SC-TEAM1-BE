package com.gdg.Todak.member.domain;

import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("중복된 유저명으로 생성할 수 없다.")
    @Test
    void duplicateUsernameTest() {
        // given
        String username = "username";

        Member member1 = Member.of(username, "password", "imageUrl", "salt");
        Member member2 = Member.of(username, "password", "imageUrl", "salt");

        // when
        memberRepository.save(member1);

        // then
        assertThatThrownBy(() -> memberRepository.save(member2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}