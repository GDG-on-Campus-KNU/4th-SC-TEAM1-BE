package com.gdg.Todak.point.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PointFilterDateRange {
    private Instant start;
    private Instant end;

    public static PointFilterDateRange of(Instant start, Instant end) {
        return PointFilterDateRange.builder()
                .start(start)
                .end(end)
                .build();
    }
}
