package com.gdg.Todak.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String password;

    private String nickname;

    private String imageUrl;

    private String salt;

    private boolean aiCommentEnabled;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MemberRole> memberRoles = new HashSet<>();

    @Builder
    public Member(String userId, String password, String nickname, String imageUrl, String salt, boolean aiCommentEnabled) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.salt = salt;
        this.aiCommentEnabled = aiCommentEnabled;
    }

    @Builder
    public Member(String userId, String password, String nickname, String imageUrl, String salt) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.salt = salt;
    }

    public static Member of(String userId, String password, String nickname, String imageUrl, String salt) {
        return Member.builder()
            .userId(userId)
            .password(password)
            .nickname(nickname)
            .imageUrl(imageUrl)
            .salt(salt)
            .aiCommentEnabled(true)
            .build();
    }

    public void addRole(MemberRole memberRole) {
        memberRoles.add(memberRole);
    }

    public Set<Role> getRoles() {
        return memberRoles.stream()
            .map(MemberRole::getRole).collect(Collectors.toSet());
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void enableAiComment() {
        this.aiCommentEnabled = true;
    }

    public void disableAiComment() {
        this.aiCommentEnabled = false;
    }
}
