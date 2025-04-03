package com.gdg.Todak.point.dto;

import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;

import java.time.LocalDateTime;

public record PointLogResponse(
        PointType pointType,
        PointStatus pointStatus,
        LocalDateTime createdAt,
        int point
) {
}
