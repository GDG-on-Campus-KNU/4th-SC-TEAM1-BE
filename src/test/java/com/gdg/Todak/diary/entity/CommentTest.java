package com.gdg.Todak.diary.entity;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    private Member member;
    private Diary diary;

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


    @Test
    @DisplayName("Comment 객체 생성 테스트")
    void constructCommentTest() {
        // when
        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content("testComment")
                .build();

        // then
        assertThat(comment).isNotNull();
        assertThat(comment.getMember()).isEqualTo(member);
        assertThat(comment.getContent()).isEqualTo("testComment");
    }

    @Test
    @DisplayName("Comment update 테스트")
    void updateCommentTest() {
        // given
        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content("testComment")
                .build();

        // when
        comment.updateComment("updatedComment");

        // then
        assertThat(comment.getContent()).isEqualTo("updatedComment");
    }

    @Test
    @DisplayName("isNotWriter 테스트")
    void isNotWriterTest() {
        // given
        Member notWriter = new Member("user2", "test2", "test2", "test2", "test2");

        Comment comment = Comment.builder()
                .member(member)
                .diary(diary)
                .content("testComment")
                .build();

        // when & then
        assertThat(comment.isNotWriter(member)).isFalse();
        assertThat(comment.isNotWriter(notWriter)).isTrue();
    }
}
