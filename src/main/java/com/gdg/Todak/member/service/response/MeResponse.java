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

    public static MeResponse of(Member member) {
        return MeResponse.builder()
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .build();
    }
}
