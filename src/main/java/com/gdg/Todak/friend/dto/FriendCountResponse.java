package com.gdg.Todak.friend.dto;

import com.gdg.Todak.friend.FriendStatus;

public record FriendCountResponse(
        FriendStatus friendStatus,
        boolean isAccepter,
        boolean isRequester,
        long count
) {
}
