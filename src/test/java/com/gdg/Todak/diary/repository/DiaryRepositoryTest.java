package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DiaryRepositoryTest {

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("Diary 객체 정상 저장 테스트")
    @Test
    void diarySaveTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Diary diary = Diary.builder()
                .member(member)
                .content("오늘 하루도 행복했다.")
                .emotion(Emotion.HAPPY)
                .build();

        // when
        Diary savedDiary = diaryRepository.save(diary);

        // then
        assertThat(savedDiary.getId()).isNotNull();
        assertThat(savedDiary.getMember().getId()).isNotNull();
        assertThat(savedDiary.getContent()).isEqualTo("오늘 하루도 행복했다.");
    }

    @DisplayName("findByMemberAndCreatedAtBetween() 테스트 - 특정 기간의 일기 조회")
    @Test
    void findByMemberAndCreatedAtBetweenTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Instant start = Instant.now().minusSeconds(86400);
        Instant end = Instant.now().plusSeconds(86400);

        Diary yesterdayDiary = Diary.builder().member(member).content("어제의 일기").emotion(Emotion.HAPPY).build();
        ReflectionTestUtils.setField(yesterdayDiary, "createdAt", start);
        diaryRepository.save(yesterdayDiary);

        Diary todayDiary = Diary.builder().member(member).content("오늘의 일기").emotion(Emotion.HAPPY).build();
        ReflectionTestUtils.setField(todayDiary, "createdAt", Instant.now());
        diaryRepository.save(todayDiary);

        Diary tomorrowDiary = Diary.builder().member(member).content("내일의 일기").emotion(Emotion.HAPPY).build();
        ReflectionTestUtils.setField(tomorrowDiary, "createdAt", end);
        diaryRepository.save(tomorrowDiary);

        // when
        List<Diary> diaries = diaryRepository.findByMemberAndCreatedAtBetween(member, start, end);

        // then
        assertThat(diaries).hasSize(3);
    }

    @DisplayName("existsByMemberAndCreatedAtBetween() 테스트 - 특정 기간에 작성된 일기 존재 여부 확인")
    @Test
    void existsByMemberAndCreatedAtBetweenTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Instant startOfDay = Instant.now().minusSeconds(43200);
        Instant endOfDay = Instant.now().plusSeconds(43200);

        Diary diary = Diary.builder()
                .member(member)
                .content("오늘 하루도 행복했다.")
                .emotion(Emotion.HAPPY)
                .build();
        ReflectionTestUtils.setField(diary, "createdAt", Instant.now());

        diaryRepository.save(diary);

        // when
        boolean exists = diaryRepository.existsByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);

        // then
        assertThat(exists).isTrue();
    }
}
