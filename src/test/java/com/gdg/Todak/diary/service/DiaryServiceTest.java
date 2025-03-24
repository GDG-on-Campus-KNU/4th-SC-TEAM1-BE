package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.dto.DiarySearchRequest;
import com.gdg.Todak.diary.dto.DiarySummaryResponse;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.NotFoundException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DiaryServiceTest {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FriendRepository friendRepository;

    private Member writer;
    private Member nonWriter;

    @BeforeEach
    void setUp() {
        writer = memberRepository.save(new Member("writerUser", "test1", "test1", "test1"));
        nonWriter = memberRepository.save(new Member("nonWriterUser", "test2", "test2", "test2"));

        friendRepository.save(Friend.builder().requester(writer).accepter(nonWriter).friendStatus(FriendStatus.ACCEPTED).build());
    }

    @Test
    @DisplayName("일기 작성 성공")
    void writeDiarySuccessfullyTest() {
        // given
        DiaryRequest diaryRequest = new DiaryRequest("오늘은 기분이 좋다.", Emotion.HAPPY);

        // when
        diaryService.writeDiary(writer.getUsername(), diaryRequest);

        // then
        Optional<Diary> diary = diaryRepository.findByMemberAndContent(writer, "오늘은 기분이 좋다.");
        assertThat(diary).isPresent();
        assertThat(diary.get().getContent()).isEqualTo("오늘은 기분이 좋다.");
        assertThat(diary.get().getEmotion()).isEqualTo(Emotion.HAPPY);
    }

    @Test
    @DisplayName("하루에 이미 작성된 일기 존재 시 작성 불가")
    void cannotWriteDiaryIfAlreadyExistsTest() {
        // given
        DiaryRequest diaryRequest = new DiaryRequest("오늘은 기분이 좋다.", Emotion.HAPPY);
        diaryService.writeDiary(writer.getUsername(), diaryRequest);

        // when & then
        assertThatThrownBy(() -> diaryService.writeDiary(writer.getUsername(), diaryRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("오늘 이미 작성된 일기 또는 감정이 있습니다. 삭제 후 재작성하거나 작성된 일기를 수정해주세요.");
    }

    @Test
    @DisplayName("존재하지 않는 회원에 대한 일기 작성 시 예외 발생")
    void writeDiaryForNonExistingMemberTest() {
        // given
        DiaryRequest diaryRequest = new DiaryRequest("오늘은 기분이 좋다.", Emotion.HAPPY);

        // when & then
        assertThatThrownBy(() -> diaryService.writeDiary("nonExistentUser", diaryRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("memberName에 해당하는 멤버가 없습니다.");
    }

    @Test
    @DisplayName("자신의 일기 조회 성공")
    void readOwnDiarySuccessfullyTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        // when
        var diaryDetail = diaryService.readDiary(writer.getUsername(), diary.getId());

        // then
        assertThat(diaryDetail).isNotNull();
        assertThat(diaryDetail.content()).isEqualTo("오늘은 기분이 좋다.");
        assertThat(diaryDetail.emotion()).isEqualTo(Emotion.HAPPY);
        assertThat(diaryDetail.isWriter()).isTrue();
    }

    @Test
    @DisplayName("자신의 일기 요약 조회 성공")
    void getOwnSummaryByYearAndMonthSuccessfullyTest() {
        // given
        Instant now = Instant.now();
        int year = now.atZone(ZoneId.systemDefault()).getYear();
        int month = now.atZone(ZoneId.systemDefault()).getMonthValue();
        DiarySearchRequest diarySearchRequest = new DiarySearchRequest(year, month);

        Diary diary1 = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("자신의 3월 첫 번째 일기")
                .emotion(Emotion.HAPPY)
                .build());
        ReflectionTestUtils.setField(diary1, "createdAt", Instant.now());


        Diary diary2 = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("자신의 3월 두 번째 일기")
                .emotion(Emotion.SAD)
                .build());
        ReflectionTestUtils.setField(diary2, "createdAt", Instant.now());


        // when
        List<DiarySummaryResponse> summaries = diaryService.getMySummaryByYearAndMonth(writer.getUsername(), diarySearchRequest);

        // then
        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).emotion()).isEqualTo(Emotion.HAPPY);
        assertThat(summaries.get(1).emotion()).isEqualTo(Emotion.SAD);
    }

    @Test
    @DisplayName("친구의 일기 요약 조회 성공")
    void getFriendSummaryByYearAndMonthSuccessfullyTest() {
        // given
        Instant now = Instant.now();
        int year = now.atZone(ZoneId.systemDefault()).getYear();
        int month = now.atZone(ZoneId.systemDefault()).getMonthValue();
        DiarySearchRequest diarySearchRequest = new DiarySearchRequest(year, month);

        // 회원과 친구에 해당하는 일기 데이터 생성
        Diary diary1 = diaryRepository.save(Diary.builder()
                .member(nonWriter)
                .content("친구의 3월 첫 번째 일기")
                .emotion(Emotion.HAPPY)
                .build());
        ReflectionTestUtils.setField(diary1, "createdAt", Instant.now());


        Diary diary2 = diaryRepository.save(Diary.builder()
                .member(nonWriter)
                .content("친구의 3월 두 번째 일기")
                .emotion(Emotion.SAD)
                .build());
        ReflectionTestUtils.setField(diary2, "createdAt", Instant.now());

        // when
        List<DiarySummaryResponse> summaries = diaryService.getFriendSummaryByYearAndMonth(writer.getUsername(), nonWriter.getUsername(), diarySearchRequest);

        // then
        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).emotion()).isEqualTo(Emotion.HAPPY);
        assertThat(summaries.get(1).emotion()).isEqualTo(Emotion.SAD);
    }

    @Test
    @DisplayName("타인의 일기 조회 성공 (친구만 조회 가능)")
    void readFriendDiarySuccessfullyTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        // when
        var diaryDetail = diaryService.readDiary(nonWriter.getUsername(), diary.getId());

        // then
        assertThat(diaryDetail).isNotNull();
        assertThat(diaryDetail.content()).isEqualTo("오늘은 기분이 좋다.");
        assertThat(diaryDetail.emotion()).isEqualTo(Emotion.HAPPY);
        assertThat(diaryDetail.isWriter()).isFalse();
    }

    @Test
    @DisplayName("자신의 일기가 아니면 조회 불가")
    void cannotReadNonOwnDiaryTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        Member newMember = memberRepository.save(Member.builder()
                .username("newMember")
                .password("password")
                .salt("salt")
                .imageUrl("imageUrl")
                .build());

        // when & then
        assertThatThrownBy(() -> diaryService.readDiary(newMember.getUsername(), diary.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("작성자 또는 작성자의 친구만 일기 조회가 가능합니다.");
    }

    @Test
    @DisplayName("일기 수정 성공")
    void updateDiarySuccessfullyTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        DiaryRequest updateRequest = new DiaryRequest("오늘은 기분이 매우 좋다.", Emotion.HAPPY);

        // when
        diaryService.updateDiary(writer.getUsername(), diary.getId(), updateRequest);

        // then
        Optional<Diary> updatedDiary = diaryRepository.findById(diary.getId());
        assertThat(updatedDiary).isPresent();
        assertThat(updatedDiary.get().getContent()).isEqualTo("오늘은 기분이 매우 좋다.");
        assertThat(updatedDiary.get().getEmotion()).isEqualTo(Emotion.HAPPY);
    }

    @Test
    @DisplayName("일기 수정 시 작성자가 아니면 예외 발생")
    void cannotUpdateDiaryIfNotOwnerTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        DiaryRequest updateRequest = new DiaryRequest("오늘은 기분이 별로다.", Emotion.SAD);

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(nonWriter.getUsername(), diary.getId(), updateRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("일기 작성자가 아닙니다.");
    }

    @Test
    @DisplayName("일기 삭제 성공")
    void deleteDiarySuccessfullyTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        // when
        diaryService.deleteDiary(writer.getUsername(), diary.getId());

        // then
        Optional<Diary> deletedDiary = diaryRepository.findById(diary.getId());
        assertThat(deletedDiary).isEmpty();
    }

    @Test
    @DisplayName("일기 삭제 시 작성자가 아니면 예외 발생")
    void cannotDeleteDiaryIfNotOwnerTest() {
        // given
        Diary diary = diaryRepository.save(Diary.builder()
                .member(writer)
                .content("오늘은 기분이 좋다.")
                .emotion(Emotion.HAPPY)
                .build());

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(nonWriter.getUsername(), diary.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("일기 작성자가 아닙니다.");
    }
}
