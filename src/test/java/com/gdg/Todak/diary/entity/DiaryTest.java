package com.gdg.Todak.diary.entity;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiaryTest {

    private Diary diary;
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("user1", "test1", "test1", "test1", "test1");
        diary = Diary.builder()
                .content("오늘 하루도 힘들었다.")
                .emotion(Emotion.SAD)
                .member(member)
                .storageUUID("testUUID")
                .build();
    }

    @DisplayName("Diary 객체 생성 테스트")
    @Test
    void constructorTest() {
        assertThat(diary).isNotNull();
        assertThat(diary.getContent()).isEqualTo("오늘 하루도 힘들었다.");
        assertThat(diary.getEmotion()).isEqualTo(Emotion.SAD);
        assertThat(diary.getMember()).isEqualTo(member);
        assertThat(diary.getStorageUUID()).isEqualTo("testUUID");
    }

    @DisplayName("일기를 수정하면 내용과 감정 상태가 변경되어야 한다")
    @Test
    void updateDiaryTest() {
        // when
        diary.updateDiary("오늘은 기분이 좋다!", Emotion.HAPPY);

        // then
        assertThat(diary.getContent()).isEqualTo("오늘은 기분이 좋다!");
        assertThat(diary.getEmotion()).isEqualTo(Emotion.HAPPY);
    }

    @DisplayName("일기의 작성자인 경우 isWriter가 true를 반환해야 한다")
    @Test
    void isWriterTest() {
        assertThat(diary.isWriter(member)).isTrue();
    }

    @DisplayName("일기의 작성자가 아닌 경우 isWriter가 false를 반환해야 한다")
    @Test
    void isWriter_notWriterTest() {
        Member otherMember = new Member("user2", "test2", "test2", "test2", "test2");
        assertThat(diary.isWriter(otherMember)).isFalse();
    }
}
