package com.gdg.Todak.member.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class AuthenticateUser {

    private String userId;
    private Set<Role> roles;

    @Builder
    public AuthenticateUser(String userId, Set<Role> roles) {
        this.userId = userId;
        this.roles = roles;
    }
}
