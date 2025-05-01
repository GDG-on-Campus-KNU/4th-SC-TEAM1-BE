package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.util.AdminPageSelectBoxItems;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointFilterDateRange;
import com.gdg.Todak.point.entity.PointLog;
import com.gdg.Todak.point.repository.PointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminPageController {

    public static final String CSV_FILENAME = "point_logs.csv";
    private final PointLogRepository pointLogRepository;

    @GetMapping
    public String adminPage() {
        return "home";
    }

    @GetMapping("/point")
    public String pointAdminPage(
            Model model,
            @RequestParam(required = false) String filterCode,
            @RequestParam(required = false) String filterValue
    ) {
        List<PointLog> pointLogs = getPointLogs(filterCode, filterValue);

        model.addAttribute("pointLogs", pointLogs);

        model.addAttribute("filterCodes", AdminPageSelectBoxItems.filterCodes);

        return "point";
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPointAdminPage(
            @RequestParam(required = false) String filterCode,
            @RequestParam(required = false) String filterValue
    ) {
        List<PointLog> pointLogs = getPointLogs(filterCode, filterValue);

        byte[] csvBytes = getCsvBytes(pointLogs);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(CSV_FILENAME, StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    private static byte[] getCsvBytes(List<PointLog> pointLogs) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("id,userId,point,pointType,pointStatus,createdAt\n");

        for (PointLog log : pointLogs) {
            csvBuilder.append(String.format("%d,%s,%d,%s,%s,%s\n",
                    log.getId(),
                    log.getMember().getUserId(),
                    log.getPoint(),
                    log.getPointType(),
                    log.getPointStatus(),
                    log.getCreatedAt()
            ));
        }

        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
        return csvBytes;
    }

    private List<PointLog> getPointLogs(String filterCode, String filterValue) {
        if (isNullOrempty(filterCode) || isNullOrempty(filterValue)) {
            return pointLogRepository.findAll();
        }

        List<PointLog> pointLogs;
        switch (filterCode) {
            case "userId":
                pointLogs = pointLogRepository.findAllByMember_UserId(filterValue);
                break;
            case "pointType":
                pointLogs = pointLogRepository.findAllByPointType(parsePointType(filterValue));
                break;
            case "pointStatus":
                pointLogs = pointLogRepository.findAllByPointStatus(parsePointStatus(filterValue));
                break;
            case "date":
                PointFilterDateRange dateRange = getDateRange(filterValue);
                pointLogs = pointLogRepository.findAllByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
                break;
            default:
                pointLogs = pointLogRepository.findAll();
                break;
        }

        return pointLogs;
    }

    private static boolean isNullOrempty(String filterCode) {
        return filterCode == null || filterCode.isEmpty();
    }

    private static PointType parsePointType(String filterValue) {
        try {
            return PointType.valueOf(filterValue);
        } catch (Exception e) {
            return null;
        }
    }

    private static PointStatus parsePointStatus(String filterValue) {
        try {
            return PointStatus.valueOf(filterValue);
        } catch (Exception e) {
            return null;
        }
    }

    private static PointFilterDateRange getDateRange(String filterValue) {
        List<Integer> date = Arrays.stream(filterValue.split("-"))
                .map(value -> Integer.parseInt(value))
                .collect(Collectors.toList());
        LocalDate localDate = LocalDate.of(date.get(0), date.get(1), date.get(2));
        Instant start = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return PointFilterDateRange.of(start, end);
    }
}
