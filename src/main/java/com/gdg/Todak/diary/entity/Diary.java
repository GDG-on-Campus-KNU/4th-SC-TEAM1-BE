package com.gdg.Todak.diary.entity;

import com.gdg.Todak.common.domain.BaseEntity;
import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @Lob
    private String content;
    @NotNull
    private Emotion emotion;
    @ManyToOne
    @JoinColumn(name = "member_id")
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;
    @NotNull
    @NotEmpty
    private String storageUUID;

    public void updateDiary(String content, Emotion emotion) {
        this.content = content;
        this.emotion = emotion;
    }

    public boolean isWriter(Member member) {
        return this.member.equals(member);
    }
}
