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
    private String username;

    private String password;

    private String imageUrl;

    private String salt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MemberRole> memberRoles = new HashSet<>();

    @Builder
    public Member(String username, String password, String imageUrl, String salt) {
        this.username = username;
        this.password = password;
        this.imageUrl = imageUrl;
        this.salt = salt;
    }

    public static Member of(String username, String password, String imageUrl, String salt) {
        return Member.builder()
                .username(username)
                .password(password)
                .imageUrl(imageUrl)
                .salt(salt)
                .build();
    }

    public void addRole(MemberRole memberRole) {
        memberRoles.add(memberRole);
    }

    public Set<Role> getRoles() {
        return memberRoles.stream()
                .map(MemberRole::getRole).collect(Collectors.toSet());
    }
}
