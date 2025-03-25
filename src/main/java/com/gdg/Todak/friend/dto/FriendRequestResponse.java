package com.gdg.Todak.friend.dto;

public record FriendRequestResponse(
        Long friendRequestId,
        String requesterName,
        String accepterName
) {
}
