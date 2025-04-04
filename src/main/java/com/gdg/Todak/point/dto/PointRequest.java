package com.gdg.Todak.point.dto;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointType;

public record PointRequest(
        Member member,
        PointType pointType
) {
}
