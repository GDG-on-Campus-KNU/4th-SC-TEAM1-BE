package com.gdg.Todak.member.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class AuthenticateUser {

    private String username;
    private Set<Role> roles;

    @Builder
    public AuthenticateUser(String username, Set<Role> roles) {
        this.username = username;
        this.roles = roles;
    }
}
