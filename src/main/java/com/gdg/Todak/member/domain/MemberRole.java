package com.gdg.Todak.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MemberRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    private Role role;

    @Builder
    public MemberRole(Member member, Role role) {
        this.member = member;
        this.role = role;
    }

    public static MemberRole of(Role role, Member member) {
        return MemberRole.builder()
                .role(role)
                .member(member)
                .build();
    }
}
