package com.gdg.Todak.guestbook.service;

import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookResponse;
import com.gdg.Todak.guestbook.controller.dto.DeleteGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.GetGuestbookResponse;
import com.gdg.Todak.guestbook.entity.Guestbook;
import com.gdg.Todak.guestbook.exception.NotFoundException;
import com.gdg.Todak.guestbook.exception.UnauthorizedException;
import com.gdg.Todak.guestbook.repository.GuestbookRepository;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
class GuestbookServiceTest {

    @Autowired
    GuestbookService guestbookService;
    @Autowired
    GuestbookRepository guestbookRepository;
    @Autowired
    MemberRepository memberRepository;
    @MockitoBean
    FriendCheckService friendCheckService;

    String testContent;
    String senderUserId;
    String receiverUserId;

    Member sender;
    Member receiver;


    @BeforeEach
    void setUp() {
        testContent = "testContent";

        senderUserId = "userId1";
        sender = Member.of(senderUserId, "password", "nickname1", "imageUrl", "salt");

        receiverUserId = "userId2";
        receiver = Member.of(receiverUserId, "password", "nickname2", "imageUrl", "salt");

        memberRepository.saveAll(List.of(sender, receiver));
    }

    @DisplayName("방명록을 조회한다.")
    @Test
    void getGuestbookTest() {
        // given
        Guestbook guestbook = new Guestbook(sender, receiver, testContent, Instant.now().plus(1, ChronoUnit.DAYS));
        guestbookRepository.save(guestbook);

        AuthenticateUser authenticateUser = AuthenticateUser.of(receiverUserId, Set.of(Role.USER));

        // when
        List<GetGuestbookResponse> findGuestbooks = guestbookService.getMyGuestbook(authenticateUser);

        // then
        assertThat(findGuestbooks).hasSize(1)
                .extracting(GetGuestbookResponse::getContent)
                .containsExactly(testContent);
    }

    @DisplayName("친구 방명록을 조회한다.")
    @Test
    void getFriendGuestbookTest() {
        // given
        Guestbook guestbook = new Guestbook(sender, receiver, testContent, Instant.now().plus(1, ChronoUnit.DAYS));
        guestbookRepository.save(guestbook);

        AuthenticateUser authenticateUser = AuthenticateUser.of(senderUserId, Set.of(Role.USER));

        when(friendCheckService.getFriendMembers(anyString())).thenReturn(List.of(sender));

        // when
        List<GetGuestbookResponse> findGuestbooks = guestbookService.getFriendGuestbook(authenticateUser, receiverUserId);

        // then
        assertThat(findGuestbooks).hasSize(1)
                .extracting(GetGuestbookResponse::getContent)
                .containsExactly(testContent);
    }

    @DisplayName("방명록을 추가한다.")
    @Test
    void addGuestbookTest() {
        // given
        AddGuestbookRequest request = AddGuestbookRequest.of(receiverUserId, testContent);

        AuthenticateUser authenticateUser = AuthenticateUser.of(senderUserId, Set.of(Role.USER));

        when(friendCheckService.getFriendMembers(anyString())).thenReturn(List.of(sender));

        // when
        AddGuestbookResponse response = guestbookService.addGuestbook(authenticateUser, request);

        // then
        assertThat(response.getContent()).isEqualTo(testContent);
    }

    @DisplayName("방명록을 삭제한다.")
    @Test
    void deleteGuestbookTest() {
        // given
        Guestbook guestbook = new Guestbook(sender, receiver, testContent, Instant.now().plus(1, ChronoUnit.DAYS));
        Guestbook saveGuestbook = guestbookRepository.save(guestbook);

        DeleteGuestbookRequest request = DeleteGuestbookRequest.of(saveGuestbook.getId());

        AuthenticateUser authenticateUser = AuthenticateUser.of(receiverUserId, Set.of(Role.USER));

        // when
        guestbookService.deleteGuestbook(authenticateUser, request);

        Optional<Guestbook> findGuestbook = guestbookRepository.findById(saveGuestbook.getId());

        // then
        assertThat(findGuestbook).isEmpty();
    }

    @DisplayName("존재하지 않는 방명록을 삭제하면 예외가 발생한다.")
    @Test
    void deleteNonexistentGuestbookTest() {
        // given
        DeleteGuestbookRequest request = DeleteGuestbookRequest.of(-1L);

        AuthenticateUser authenticateUser = AuthenticateUser.of(receiverUserId, Set.of(Role.USER));

        // when // then
        assertThatThrownBy(() -> guestbookService.deleteGuestbook(authenticateUser, request))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("방명록 주인이 아닌 사람이 방명록을 삭제하면 예외가 발생한다.")
    @Test
    void deleteByNotOwnerGuestbookTest() {
        // given
        Guestbook guestbook = new Guestbook(sender, receiver, testContent, Instant.now().plus(1, ChronoUnit.DAYS));
        Guestbook saveGuestbook = guestbookRepository.save(guestbook);

        DeleteGuestbookRequest request = DeleteGuestbookRequest.of(saveGuestbook.getId());

        AuthenticateUser notOwnerAuthenticateUser = AuthenticateUser.of(senderUserId, Set.of(Role.USER));

        // when // then
        assertThatThrownBy(() -> guestbookService.deleteGuestbook(notOwnerAuthenticateUser, request))
                .isInstanceOf(UnauthorizedException.class);
    }

}
