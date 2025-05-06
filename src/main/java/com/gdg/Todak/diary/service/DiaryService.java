package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.dto.*;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.NotFoundException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.notification.service.NotificationService;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final NotificationService notificationService;
    private final FriendCheckService friendCheckService;
    private final PointService pointService;
    private final SchedulerService schedulerService;

    @Transactional
    public void writeDiary(String userId, DiaryRequest diaryRequest) {
        Member member = getMember(userId);

        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(23, 59, 59, 99).atZone(ZoneId.systemDefault()).toInstant();

        if (diaryRepository.existsByMemberAndCreatedAtBetween(member, startOfDay, endOfDay)) {
            throw new BadRequestException("오늘 이미 작성된 일기 또는 감정이 있습니다. 삭제 후 재작성하거나 작성된 일기를 수정해주세요.");
        }

        Diary diary = Diary.builder()
                .member(member)
                .content(diaryRequest.content())
                .emotion(diaryRequest.emotion())
                .storageUUID(diaryRequest.storageUUID())
                .build();

        Diary saveDiary = diaryRepository.save(diary);

        pointService.earnPointByType(new PointRequest(member, PointType.DIARY));

        // AI 댓글 작성 예약
        schedulerService.scheduleSavingCommentByAI(saveDiary);

        // 알림 전송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationService.publishPostNotifications(userId, "post", saveDiary.getId());
            }
        });
    }

    public List<DiarySummaryResponse> getMySummaryByYearAndMonth(String userId, DiarySearchRequest diarySearchRequest) {
        Member member = getMember(userId);

        int year = diarySearchRequest.year();
        int month = diarySearchRequest.month();

        if (month < 1 || month > 12) {
            throw new BadRequestException("month의 범위는 1~12 입니다.");
        }

        List<Diary> diaries = getDiariesByYearAndMonth(year, month, member);

        if (!diaries.isEmpty() && !diaries.getFirst().isWriter(member)) {
            throw new UnauthorizedException("일기 작성자가 아닙니다.");
        }

        return diaries.stream()
                .map(diary -> new DiarySummaryResponse(
                        diary.getId(),
                        diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        diary.getEmotion()
                )).toList();
    }

    public List<DiarySummaryResponse> getFriendSummaryByYearAndMonth(String userId, String friendId, DiarySearchRequest diarySearchRequest) {
        Member friendMember = getMember(friendId);

        int year = diarySearchRequest.year();
        int month = diarySearchRequest.month();

        if (month < 1 || month > 12) {
            throw new BadRequestException("month의 범위는 1~12 입니다.");
        }

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(userId);

        if (!acceptedMembers.contains(friendMember)) {
            throw new UnauthorizedException("친구만 조회 가능합니다.");
        }

        List<Diary> diaries = getDiariesByYearAndMonth(year, month, friendMember);

        return diaries.stream()
                .map(diary -> new DiarySummaryResponse(
                        diary.getId(),
                        diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        diary.getEmotion()
                )).toList();
    }

    private List<Diary> getDiariesByYearAndMonth(int year, int month, Member member) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(99);

        Instant startInstant = startOfMonth.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endOfMonth.atZone(ZoneId.systemDefault()).toInstant();

        return diaryRepository.findByMemberAndCreatedAtBetween(member, startInstant, endInstant);
    }

    public DiaryDetailResponse readDiary(String userId, Long diaryId) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("diary id에 해당하는 일기가 없습니다."));

        LocalDateTime createdAt = diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (diary.isWriter(member)) {
            return new DiaryDetailResponse(diary.getId(), createdAt, diary.getContent(), diary.getEmotion(), diary.getStorageUUID(), true);
        }

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(diary.getMember().getUserId());

        if (!acceptedMembers.contains(member)) {
            throw new UnauthorizedException("작성자 또는 작성자의 친구만 일기 조회가 가능합니다.");
        }

        return new DiaryDetailResponse(diary.getId(), createdAt, diary.getContent(), diary.getEmotion(), diary.getStorageUUID(), false);
    }

    @Transactional
    public void updateDiary(String userId, Long diaryId, DiaryUpdateRequest diaryUpdateRequest) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("diary id에 해당하는 일기가 없습니다."));

        if (!diary.isWriter(member)) {
            throw new UnauthorizedException("일기 작성자가 아닙니다.");
        }

        diary.updateDiary(diaryUpdateRequest.content(), diaryUpdateRequest.emotion());
    }

    @Transactional
    public void deleteDiary(String userId, Long diaryId) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("diary id에 해당하는 일기가 없습니다."));

        if (!diary.isWriter(member)) {
            throw new UnauthorizedException("일기 작성자가 아닙니다.");
        }

        imageService.deleteAllImagesInStorageUUID(userId, diary.getStorageUUID());
        diaryRepository.delete(diary);
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
