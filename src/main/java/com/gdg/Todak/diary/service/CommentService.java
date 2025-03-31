package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.dto.AICommentRequest;
import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.dto.CommentResponse;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.NotFoundException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import com.gdg.Todak.diary.repository.CommentRepository;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final FriendCheckService friendCheckService;

    public Page<CommentResponse> getComments(String userId, Long diaryId, Pageable pageable) {
        Member member = getMember(userId);
        Diary diary = getDiary(diaryId);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(diary.getMember().getUserId());

        if (!diary.isWriter(member) && !acceptedMembers.contains(member)) {
            throw new UnauthorizedException("해당 일기의 댓글을 조회할 권한이 없습니다. 일기 작성자가 본인이거나, 친구일 경우에만 조회가 가능합니다.");
        }

        return commentRepository.findAllByDiary(diary, pageable)
                .map(
                        Comment -> new CommentResponse(
                                Comment.getId(),
                                Comment.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                                Comment.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                                Comment.getMember().getNickname(),
                                Comment.getContent(),
                                Comment.getMember().equals(member)
                        ));
    }

    @Transactional
    public void saveComment(String userId, Long diaryId, CommentRequest commentRequest) {
        Member member = getMember(userId);
        Diary diary = getDiary(diaryId);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(diary.getMember().getUserId());

        if (!diary.isWriter(member) && !acceptedMembers.contains(member)) {
            throw new UnauthorizedException("해당 일기에 댓글을 작성할 권한이 없습니다. 본인이거나 친구일 경우에만 작성이 가능합니다.");
        }

        Comment comment = Comment.builder()
                .member(member)
                .content(commentRequest.content())
                .diary(diary)
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public void updateComment(String userId, Long commentId, CommentRequest commentRequest) {
        Member member = getMember(userId);
        Comment comment = getComment(commentId);

        if (!comment.getDiary().isWriter(member)) {
            throw new UnauthorizedException("해당 댓글을 수정할 권한이 없습니다.");
        }

        comment.updateComment(commentRequest.content());
    }

    @Transactional
    public void deleteComment(String userId, Long commentId) {
        Member member = getMember(userId);
        Comment comment = getComment(commentId);

        if (!comment.getDiary().isWriter(member)) {
            throw new UnauthorizedException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("commentId에 해당하는 댓글이 없습니다."));
    }

    private Diary getDiary(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("diaryId에 해당하는 일기가 없습니다."));
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }

    @Transactional
    public void saveCommentByAI(Diary diary, AICommentRequest aiCommentRequest) {
        // AI 파트에서 사용할 댓글 저장용 메서드
        Comment commentByAI = Comment.builder()
                .member(aiCommentRequest.member())
                .content(aiCommentRequest.content())
                .diary(diary)
                .build();

        commentRepository.save(commentByAI);
    }
}
