package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @DisplayName("Comment객체 정상 저장 테스트")
    @Test
    void saveTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Diary diary = diaryRepository.save(
                Diary.builder()
                        .member(member)
                        .content("오늘 하루도 행복했다.")
                        .emotion(Emotion.HAPPY)
                        .storageUUID("testUUID")
                        .build());

        // when
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .diary(diary)
                .content("testComment")
                .build());

        // then
        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getMember().getId()).isEqualTo(member.getId());
        assertThat(comment.getDiary().getId()).isEqualTo(diary.getId());
        assertThat(comment.getContent()).isEqualTo("testComment");
    }

    @DisplayName("Diary의 모든 Comment들 Page로 불러오기 테스트")
    @Test
    void findAllByDiaryTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Diary diary = diaryRepository.save(
                Diary.builder()
                        .member(member)
                        .content("오늘 하루도 행복했다.")
                        .emotion(Emotion.HAPPY)
                        .storageUUID("testUUID")
                        .build());
        for (int i = 0; i < 5; i++) {
            Comment comment = Comment.builder()
                    .member(member)
                    .diary(diary)
                    .content("testComment " + i)
                    .build();

            commentRepository.save(comment);
        }

        // when
        Pageable pageable = Pageable.ofSize(3);
        Page<Comment> comments = commentRepository.findAllByDiary(diary, pageable);

        // then
        assertThat(comments.getTotalElements()).isEqualTo(5);
        assertThat(comments.getTotalPages()).isEqualTo(2);
        assertThat(comments.get().findFirst().isPresent()).isTrue();
        assertThat(comments.get().findFirst().get().getContent()).isEqualTo("testComment 0");
    }
}
