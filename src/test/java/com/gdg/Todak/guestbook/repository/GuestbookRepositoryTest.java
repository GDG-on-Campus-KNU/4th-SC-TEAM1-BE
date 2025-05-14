package com.gdg.Todak.guestbook.repository;

import com.gdg.Todak.guestbook.entity.Guestbook;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class GuestbookRepositoryTest {

    @Autowired
    GuestbookRepository guestbookRepository;
    @Autowired
    MemberRepository memberRepository;

    @DisplayName("receiver의 userId에 해당하는 방명록들을 모두 가져온다.")
    @Test
    void findValidGuestbooksByReceiverUserIdTest() {
        // given
        Member sender = Member.of("userId1", "password", "nickname1", "imageUrl", "salt");

        String receiverUserId = "userId2";
        Member receiver = Member.of(receiverUserId, "password", "nickname2", "imageUrl", "salt");

        memberRepository.saveAll(List.of(sender, receiver));

        String testContent = "test_content";

        Instant now = Instant.now();
        Guestbook guestbook1 = new Guestbook(sender, receiver, testContent, now.plus(1, ChronoUnit.DAYS));
        Guestbook guestbook2 = new Guestbook(sender, receiver, testContent, now.plus(-1, ChronoUnit.DAYS));

        guestbookRepository.saveAll(List.of(guestbook1, guestbook2));

        // when
        List<Guestbook> findGuestbooks = guestbookRepository.findValidGuestbooksByReceiverUserId(receiverUserId);

        // then
        assertThat(findGuestbooks).hasSize(1)
            .extracting(Guestbook::getContent)
            .containsExactly(testContent);
    }

    @DisplayName("만료된 방명록들을 모두 삭제한다.")
    @Test
    void deleteAllExpiredGuestbooksTest() {
        // given
        Member sender = Member.of("userId1", "password", "nickname1", "imageUrl", "salt");

        Member receiver = Member.of("userId2", "password", "nickname2", "imageUrl", "salt");

        memberRepository.saveAll(List.of(sender, receiver));

        String testContent = "test_content";

        Instant now = Instant.now();
        Guestbook guestbook1 = new Guestbook(sender, receiver, testContent, now.plus(-1, ChronoUnit.DAYS));
        Guestbook guestbook2 = new Guestbook(sender, receiver, testContent, now.plus(-1, ChronoUnit.DAYS));

        guestbookRepository.saveAll(List.of(guestbook1, guestbook2));

        // when
        guestbookRepository.deleteAllExpiredGuestbooks(now);

        // then
        List<Guestbook> guestbooks = guestbookRepository.findAll();
        assertThat(guestbooks).hasSize(0);
    }

}