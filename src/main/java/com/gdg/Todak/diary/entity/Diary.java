package com.gdg.Todak.diary.entity;

import com.gdg.Todak.common.domain.BaseEntity;
import com.gdg.Todak.diary.Emotion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Lob
    private String content;
    private Emotion emotion;
//    @ManyToOne
//    @JoinColumn(name = "member_id")
//    @NotNull
//    private Member member;

    public void updateDiary(String title, String content, Emotion emotion) {
        this.title = title;
        this.content = content;
        this.emotion = emotion;
    }
}
