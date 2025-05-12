package com.gdg.Todak.diary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.common.config.AiModelConfig;
import com.gdg.Todak.diary.dto.AICommentByGeminiResponse;
import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.dto.CommentResponse;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.CommentAnonymousReveal;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.NotFoundException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import com.gdg.Todak.diary.repository.CommentAnonymousRevealRepository;
import com.gdg.Todak.diary.repository.CommentRepository;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.diary.util.MBTISelector;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.notification.service.NotificationService;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZoneId;
import java.util.List;

import static com.gdg.Todak.diary.util.AiCommentPrompt.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    public static final String AI_MEMBER_USER_ID = "토닥이";

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final CommentAnonymousRevealRepository commentAnonymousRevealRepository;
    private final FriendCheckService friendCheckService;
    private final NotificationService notificationService;
    private final PointService pointService;
    private final MBTISelector mbtiSelector;
    private final AiModelConfig aiModelConfig;
    private final ObjectMapper objectMapper;

    private ChatLanguageModel model;

    @PostConstruct
    public void init() {
        this.model = aiModelConfig.geminiChatModel();
    }

    public Page<CommentResponse> getComments(String userId, Long diaryId, Pageable pageable) {
        Member member = getMember(userId);
        Diary diary = getDiary(diaryId);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(diary.getMember().getUserId());

        if (!diary.isWriter(member) && !acceptedMembers.contains(member)) {
            throw new UnauthorizedException("해당 일기의 댓글을 조회할 권한이 없습니다. 일기 작성자가 본인이거나, 친구일 경우에만 조회가 가능합니다.");
        }

        Page<Comment> comments = commentRepository.findAllByDiary(diary, pageable);

        List<CommentResponse> commentResponses = comments.getContent().stream()
                .map(comment -> {
                    boolean isWriter = !comment.isNotWriter(member);
                    boolean isRevealed = isCommentRevealed(member, comment);

                    String displayNickname;
                    String displayUserId;
                    boolean isAnonymous = true;

                    if (isWriter || isRevealed) {
                        displayNickname = comment.getMember().getNickname();
                        displayUserId = comment.getMember().getUserId();
                        isAnonymous = false;
                    } else {
                        displayNickname = "익명의 닉네임";
                        displayUserId = "익명의 아이디";
                    }

                    return CommentResponse.of(
                            comment.getId(),
                            comment.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            comment.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                            displayNickname,
                            displayUserId,
                            comment.getContent(),
                            isWriter,
                            isAnonymous
                    );
                }).toList();

        return new PageImpl<>(commentResponses, pageable, comments.getTotalElements());
    }

    private boolean isCommentRevealed(Member member, Comment comment) {
        return commentAnonymousRevealRepository.existsByMemberAndComment(member, comment);
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

        pointService.earnPointByType(new PointRequest(member, PointType.COMMENT));

        String senderId = userId;
        String receiverId = diary.getMember().getUserId();
        // 알림 전송
        if (!senderId.equals(receiverId)) { // 자신에게 자신이 댓글 알림을 보내는 것이 아닌 경우에만 알림 전송
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.publishCommentNotification(senderId, receiverId, "comment", diary.getId());
                }
            });
        }
    }

    @Transactional
    public void updateComment(String userId, Long commentId, CommentRequest commentRequest) {
        Member member = getMember(userId);
        Comment comment = getComment(commentId);

        if (comment.isNotWriter(member)) {
            throw new UnauthorizedException("해당 댓글을 수정할 권한이 없습니다.");
        }

        comment.updateComment(commentRequest.content());
    }

    @Transactional
    public void deleteComment(String userId, Long commentId) {
        Member member = getMember(userId);
        Comment comment = getComment(commentId);

        if (comment.isNotWriter(member)) {
            throw new UnauthorizedException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public String revealAnonymous(String userId, Long commentId) {
        Member member = getMember(userId);
        Comment comment = getComment(commentId);

        if (commentAnonymousRevealRepository.existsByMemberAndComment(member, comment)) {
            return "이미 해당 댓글의 익명을 해제했습니다.";
        }

        if (comment.getMember().equals(member)) {
            throw new BadRequestException("본인이 작성한 댓글은 익명 해제가 필요하지 않습니다.");
        }

        pointService.consumePointToGetCommentWriterId(member);

        commentAnonymousRevealRepository.save(CommentAnonymousReveal.of(member, comment));

        return "[Comment id :" + comment.getId() + "]에 해당하는 댓글의 익명이 해제되었습니다.";
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
    public void saveCommentByAI(Diary diary) {
        Member aiMember = getMember(AI_MEMBER_USER_ID);
        String aiComment = createAIComment(diary.getContent());

        Comment commentByAI = Comment.builder()
                .member(aiMember)
                .content(aiComment)
                .diary(diary)
                .build();

        commentRepository.save(commentByAI);
    }

    public String createAIComment(String diaryContent) {
        try {
            UserMessage prompt = createPrompt(diaryContent);

            String response = model.chat(prompt).aiMessage().text();

            String jsonString = response.replace("```json", "").replace("```", "").trim();

            AICommentByGeminiResponse parsedResponse = objectMapper.readValue(jsonString, AICommentByGeminiResponse.class);

            return parsedResponse.getComment();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private UserMessage createPrompt(String diaryContent) {
        String basePrompt = MBTI_AI_COMMENT_PROMPT;

        String mbti = mbtiSelector.select();

        String type1 = mbti.charAt(0) == 'E' ? E_STYLE : I_STYLE;
        String type2 = mbti.charAt(1) == 'S' ? S_STYLE : N_STYLE;
        String type3 = mbti.charAt(2) == 'T' ? T_STYLE : F_STYLE;
        String type4 = mbti.charAt(3) == 'J' ? J_STYLE : P_STYLE;

        String formattedPrompt = basePrompt.formatted(mbti, type1, type2, type3, type4, diaryContent);

        return new UserMessage(formattedPrompt);
    }
}
