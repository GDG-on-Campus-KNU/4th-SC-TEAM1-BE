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
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class GuestbookService {

    public static final int EXPIRE_DAY = 1;

    private final GuestbookRepository guestbookRepository;
    private final MemberRepository memberRepository;
    private final FriendCheckService friendCheckService;

    public List<GetGuestbookResponse> getGuestbook(AuthenticateUser user) {
        Member member = getMember(user.getUserId());

        return guestbookRepository.findValidGuestbooksByReceiverUserId(member.getUserId()).stream()
                .map(GetGuestbookResponse::of)
                .toList();
    }

    @Transactional
    public AddGuestbookResponse addGuestbook(AuthenticateUser user, AddGuestbookRequest request) {
        Member sender = getMember(user.getUserId());
        Member receiver = getMember(request.getUserId());

        Instant expiresAt = Instant.now().plus(EXPIRE_DAY, ChronoUnit.DAYS);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(receiver.getUserId());

        if (!acceptedMembers.contains(sender)) {
            throw new UnauthorizedException("해당 일기에 댓글을 작성할 권한이 없습니다. 본인이거나 친구일 경우에만 작성이 가능합니다.");
        }

        Guestbook guestbook = Guestbook.of(sender, receiver, request.getContent(), expiresAt);

        return AddGuestbookResponse.from(guestbookRepository.save(guestbook));
    }

    @Transactional
    public String deleteGuestbook(AuthenticateUser user, DeleteGuestbookRequest request) {
        Member member = getMember(user.getUserId());

        Guestbook guestbook = getGuestbook(request);

        if (isNotGuestbookOwner(member, guestbook)) {
            throw new UnauthorizedException("방명록 주인이 아닙니다.");
        }

        guestbookRepository.delete(guestbook);

        return "방명록이 삭제되었습니다.";
    }

    @Transactional
    public void deleteExpiredGuestbooks(Instant now) {
        guestbookRepository.deleteAllExpiredGuestbooks(now);
    }

    private static boolean isNotGuestbookOwner(Member sender, Guestbook guestbook) {
        return !sender.getUserId().equals(guestbook.getReceiver().getUserId());
    }

    private Guestbook getGuestbook(DeleteGuestbookRequest request) {
        Optional<Guestbook> guestbookOptional = guestbookRepository.findById(request.getGuestbookId());

        if (guestbookOptional.isEmpty()) {
            throw new NotFoundException("존재하지 않는 방명록 입니다.");
        }

        return guestbookOptional.get();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("멤버가 존재하지 않습니다."));
    }
}
