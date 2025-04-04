package com.gdg.Todak.point.dto;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;

import java.time.LocalDateTime;

public record PointLogRequest(
        Member member,
        int point,
        PointType pointType,
        PointStatus pointStatus,
        LocalDateTime createdAt
) {
}
