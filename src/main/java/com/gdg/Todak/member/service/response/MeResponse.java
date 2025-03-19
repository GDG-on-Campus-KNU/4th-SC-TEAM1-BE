package com.gdg.Todak.member.service.response;

import com.gdg.Todak.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeResponse {

    private String username;
    private String imageUrl;

    @Builder
    public MeResponse(String username, String imageUrl) {
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public static MeResponse of(Member member) {
        return MeResponse.builder()
                .username(member.getUsername())
                .imageUrl(member.getImageUrl())
                .build();
    }
}
