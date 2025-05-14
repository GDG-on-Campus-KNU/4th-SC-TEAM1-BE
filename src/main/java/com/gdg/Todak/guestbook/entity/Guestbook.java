package com.gdg.Todak.guestbook.entity;

import com.gdg.Todak.common.domain.BaseEntity;
import com.gdg.Todak.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class Guestbook extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member sender;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member receiver;

    private String content;

    private Instant expiresAt;

    @Builder
    public Guestbook(Member sender, Member receiver, String content, Instant expiresAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.expiresAt = expiresAt;
    }

    public static Guestbook of(Member sender, Member receiver, String content, Instant expiresAt) {
        return Guestbook.builder()
            .sender(sender)
            .receiver(receiver)
            .content(content)
            .expiresAt(expiresAt)
            .build();
    }
}
