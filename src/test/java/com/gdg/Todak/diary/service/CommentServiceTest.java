package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.dto.CommentResponse;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.CommentAnonymousReveal;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import com.gdg.Todak.diary.repository.CommentAnonymousRevealRepository;
import com.gdg.Todak.diary.repository.CommentRepository;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private CommentAnonymousRevealRepository commentAnonymousRevealRepository;

    @Autowired
    private FriendCheckService friendCheckService;

    @MockitoBean
    private PointService pointService;

    private Member member;
    private Member friendMember;
    private Member notFriendMember;
    private Diary diary;
    private Diary diaryWrittenByFriend;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("member", "test", "test", "test", "test"));
        friendMember = memberRepository.save(new Member("friendMember", "test1", "test1", "test1", "test1"));
        notFriendMember = memberRepository.save(new Member("notFriendMember", "test2", "test2", "test2", "test2"));

        friendRepository.save(Friend.builder().requester(member).accepter(friendMember).friendStatus(FriendStatus.ACCEPTED).build());

        diary = diaryRepository.save(Diary.builder().member(member).emotion(Emotion.HAPPY).storageUUID("testUUID").content("오늘의 일기").build());
        diaryWrittenByFriend = diaryRepository.save(Diary.builder().member(friendMember).emotion(Emotion.SAD).storageUUID("testUUID2").content("친구의 일기").build());

        comment = commentRepository.save(Comment.builder()
                .member(friendMember)
                .diary(diary)
                .content("댓글 내용")
                .build());

        doNothing().when(pointService).earnPointByType(any(PointRequest.class));
        doNothing().when(pointService).consumePointToGetCommentWriterId(any(Member.class));
    }

    @DisplayName("내가 작성한 일기에 댓글 작성 테스트")
    @Test
    void saveCommentToDiaryWrittenByMeTest() {
        // given
        String testContent = "testContent";
        CommentRequest commentRequest = new CommentRequest(testContent);

        // when
        commentService.saveComment(member.getUserId(), diary.getId(), commentRequest);

        // then
        Optional<Comment> comment = commentRepository.findByDiaryAndContent(diary, testContent);
        assertThat(comment).isPresent();
        assertThat(comment.get().getId()).isNotNull();
        assertThat(comment.get().getMember()).isEqualTo(member);
        assertThat(comment.get().getContent()).isEqualTo(testContent);
    }

    @DisplayName("친구가 작성한 일기에 댓글 작성 테스트")
    @Test
    void saveCommentToDiaryWrittenByFriendTest() {
        // given
        String testContent = "testContent";
        CommentRequest commentRequest = new CommentRequest(testContent);

        // when
        commentService.saveComment(member.getUserId(), diaryWrittenByFriend.getId(), commentRequest);

        // then
        Optional<Comment> comment = commentRepository.findByDiaryAndContent(diaryWrittenByFriend, testContent);
        assertThat(comment).isPresent();
        assertThat(comment.get().getDiary().getMember()).isEqualTo(friendMember);
        assertThat(comment.get().getId()).isNotNull();
        assertThat(comment.get().getMember()).isEqualTo(member);
        assertThat(comment.get().getContent()).isEqualTo(testContent);
    }

    @DisplayName("친구가 아닌 사람이 작성한 일기에 댓글 작성시 예외 테스트")
    @Test
    void saveCommentToDiaryNotWrittenByFriendTest() {
        // given
        String testContent = "testContent";
        CommentRequest commentRequest = new CommentRequest(testContent);
        Diary diaryWrittenByNotFriend = diaryRepository.save(Diary.builder().member(notFriendMember).emotion(Emotion.EXCITED).storageUUID("testUUID3").content("친구가 아닌 사람의 일기").build());

        // when & then
        assertThatThrownBy(() -> commentService.saveComment(member.getUserId(), diaryWrittenByNotFriend.getId(), commentRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("해당 일기에 댓글을 작성할 권한이 없습니다. 본인이거나 친구일 경우에만 작성이 가능합니다.");
    }

    @DisplayName("내가 쓴 일기에 달린 댓글 조회 테스트")
    @Test
    void readCommentsByDiaryWrittenByMeTest() {
        // given
        Comment comment1 = commentRepository.save(Comment.builder().diary(diary).member(member).content("내가 쓴 댓글").build());

        // when
        Page<CommentResponse> comments = commentService.getComments(member.getUserId(), diary.getId(), Pageable.ofSize(2));

        // then
        assertThat(comments.getContent().get(0).content()).isEqualTo(comment.getContent());
        assertThat(comments.getContent().get(0).isWriter()).isFalse();
        assertThat(comments.getContent().get(1).content()).isEqualTo(comment1.getContent());
        assertThat(comments.getContent().get(1).isWriter()).isTrue();
    }

    @DisplayName("친구가 쓴 일기에 달린 댓글 조회 테스트")
    @Test
    void readCommentsByDiaryWrittenByFriendTest() {
        // given
        Comment comment1 = commentRepository.save(Comment.builder().diary(diaryWrittenByFriend).member(member).content("내가 쓴 댓글").build());
        Comment comment2 = commentRepository.save(Comment.builder().diary(diaryWrittenByFriend).member(friendMember).content("친구가 쓴 댓글").build());

        // when
        Page<CommentResponse> comments = commentService.getComments(member.getUserId(), diaryWrittenByFriend.getId(), Pageable.ofSize(2));

        // then
        assertThat(comments.getTotalElements()).isEqualTo(2);
        assertThat(comments.getContent().get(0).content()).isEqualTo(comment1.getContent());
        assertThat(comments.getContent().get(0).isWriter()).isTrue();
        assertThat(comments.getContent().get(1).content()).isEqualTo(comment2.getContent());
        assertThat(comments.getContent().get(1).isWriter()).isFalse();
    }

    @DisplayName("친구가 아닌 사람이 쓴 일기에 달린 댓글 조회시 예외 테스트")
    @Test
    void readCommentsByDiaryWrittenByNotFriendTest() {
        // given
        Diary diaryWrittenByNotFriend = diaryRepository.save(Diary.builder().member(notFriendMember).emotion(Emotion.EXCITED).storageUUID("testUUID3").content("친구가 아닌 사람의 일기").build());

        commentRepository.save(Comment.builder().diary(diaryWrittenByNotFriend).member(notFriendMember).content("친구가 아닌 사람이 쓴 댓글").build());

        // when & then
        assertThatThrownBy(() -> commentService.getComments(member.getUserId(), diaryWrittenByNotFriend.getId(), Pageable.ofSize(1)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("해당 일기의 댓글을 조회할 권한이 없습니다. 일기 작성자가 본인이거나, 친구일 경우에만 조회가 가능합니다.");
    }

    @DisplayName("내가 쓴 댓글 수정 테스트")
    @Test
    void updateCommentWrittenByMeTest() {
        // given
        Comment comment = commentRepository.save(Comment.builder().diary(diary).member(member).content("내가 쓴 댓글").build());
        CommentRequest commentRequest = new CommentRequest("수정된 댓글");

        // when
        commentService.updateComment(member.getUserId(), comment.getId(), commentRequest);

        // then
        assertThat(comment.getMember()).isEqualTo(member);
        assertThat(comment.getContent()).isEqualTo(commentRequest.content());
    }

    @DisplayName("다른 사람이 쓴 댓글 수정시 예외 테스트")
    @Test
    void updateCommentWrittenByNotMeTest() {
        // given
        Comment commentWrittenByNotMe = commentRepository.save(Comment.builder().diary(diary).member(friendMember).content("친구가 쓴 댓글").build());
        CommentRequest commentRequest = new CommentRequest("수정된 댓글");

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(member.getUserId(), commentWrittenByNotMe.getId(), commentRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("해당 댓글을 수정할 권한이 없습니다.");
    }

    @DisplayName("댓글 삭제 테스트")
    @Test
    void deleteCommentTest() {
        // given
        Comment comment = commentRepository.save(Comment.builder().diary(diary).member(member).content("삭제될 댓글").build());

        // when
        commentService.deleteComment(member.getUserId(), comment.getId());

        // then
        Optional<Comment> deleteComment = commentRepository.findByDiaryAndContent(diary, comment.getContent());
        assertThat(deleteComment).isEmpty();
    }

    @Test
    @DisplayName("댓글 목록 조회 시 본인 댓글은 익명이 아니게 표시된다")
    void getComments_MyComment_NotAnonymous() {
        // given
        Comment myComment = commentRepository.save(Comment.builder()
                .member(member)
                .diary(diary)
                .content("내가 쓴 댓글")
                .build());

        Pageable pageable = Pageable.ofSize(10);

        // when
        Page<CommentResponse> result = commentService.getComments(member.getUserId(), diary.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 기존 comment + myComment

        // 자신이 작성한 댓글 찾기
        CommentResponse myResponse = result.getContent().stream()
                .filter(r -> r.content().equals("내가 쓴 댓글"))
                .findFirst()
                .orElseThrow();

        assertThat(myResponse.isWriter()).isTrue();
        assertThat(myResponse.isAnonymous()).isFalse();
        assertThat(myResponse.nickname()).isEqualTo(member.getNickname());
        assertThat(myResponse.userId()).isEqualTo(member.getUserId());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 타인의 댓글은 익명으로 표시된다")
    void getComments_OthersComment_Anonymous() {
        // given
        // 기존 comment를 사용 (friendMember가 작성한 댓글)
        Pageable pageable = Pageable.ofSize(10);

        // when
        Page<CommentResponse> result = commentService.getComments(member.getUserId(), diary.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        CommentResponse response = result.getContent().get(0);
        assertThat(response.isWriter()).isFalse();
        assertThat(response.isAnonymous()).isTrue();
        assertThat(response.nickname()).isEqualTo("익명의 닉네임");
        assertThat(response.userId()).isEqualTo("익명의 아이디");
    }

    @Test
    @DisplayName("댓글 목록 조회 시 익명 해제한 댓글은 실명으로 표시된다")
    void getComments_RevealedComment_NotAnonymous() {
        // given
        // 익명 해제 정보 저장
        commentAnonymousRevealRepository.save(CommentAnonymousReveal.of(member, comment));

        Pageable pageable = Pageable.ofSize(10);

        // when
        Page<CommentResponse> result = commentService.getComments(member.getUserId(), diary.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        CommentResponse response = result.getContent().get(0);
        assertThat(response.isWriter()).isFalse();
        assertThat(response.isAnonymous()).isFalse();
        assertThat(response.nickname()).isEqualTo(friendMember.getNickname());
        assertThat(response.userId()).isEqualTo(friendMember.getUserId());
    }

    @Test
    @DisplayName("포인트를 사용해 익명 댓글 작성자를 공개할 수 있다")
    void revealAnonymous_Success() {
        // given
        doNothing().when(pointService).consumePointToGetCommentWriterId(any(Member.class));

        // when
        String result = commentService.revealAnonymous(member.getUserId(), comment.getId());

        // then
        assertThat(result).contains("익명이 해제되었습니다");

        // 익명 해제 정보가 저장되었는지 확인
        boolean exists = commentAnonymousRevealRepository.existsByMemberAndComment(member, comment);
        assertThat(exists).isTrue();

        verify(pointService).consumePointToGetCommentWriterId(any(Member.class));
    }

    @Test
    @DisplayName("이미 익명 해제한 댓글은 다시 익명 해제할 수 없다")
    void revealAnonymous_AlreadyRevealed() {
        // given
        commentAnonymousRevealRepository.save(CommentAnonymousReveal.of(member, comment));

        // when
        String result = commentService.revealAnonymous(member.getUserId(), comment.getId());

        // then
        assertThat(result).contains("이미 해당 댓글의 익명을 해제했습니다");

        // 포인트 서비스가 호출되지 않았는지 확인
        verify(pointService, never()).consumePointToGetCommentWriterId(any(Member.class));
    }

    @Test
    @DisplayName("본인 댓글은 익명 해제가 불가하다")
    void revealAnonymous_OwnComment() {
        // given
        Comment myComment = commentRepository.save(Comment.builder()
                .member(member)
                .diary(diary)
                .content("내 댓글")
                .build());

        // when & then
        assertThatThrownBy(() -> commentService.revealAnonymous(member.getUserId(), myComment.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("본인이 작성한 댓글은 익명 해제가 필요하지 않습니다");

        verify(pointService, never()).consumePointToGetCommentWriterId(any(Member.class));
    }
}
