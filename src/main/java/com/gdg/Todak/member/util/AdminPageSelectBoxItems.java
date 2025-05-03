package com.gdg.Todak.member.util;

import com.gdg.Todak.member.util.dto.FilterCode;

import java.util.List;

public class AdminPageSelectBoxItems {

    public static List<FilterCode> filterCodes = List.of(
            new FilterCode("memberId", "멤버 ID"),
            new FilterCode("pointType", "포인트 타입"),
            new FilterCode("pointStatus", "포인트 상태"),
            new FilterCode("date", "날짜")
    );
}