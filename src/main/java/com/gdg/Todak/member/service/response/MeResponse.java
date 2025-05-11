package com.gdg.Todak.member.service.response;

import com.gdg.Todak.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeResponse {

    private String userId;
    private String nickname;
    private String imageUrl;

    @Builder
    public MeResponse(String userId, String nickname, String imageUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }

    public static MeResponse of(String userId, String nickname, String imageUrl) {
        return MeResponse.builder()
            .userId(userId)
            .nickname(nickname)
            .imageUrl(imageUrl)
            .build();
    }

    public static MeResponse from(Member member) {
        return MeResponse.of(member.getUserId(), member.getNickname(), member.getImageUrl());
    }
}
