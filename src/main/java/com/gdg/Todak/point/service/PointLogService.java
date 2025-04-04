package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.entity.PointLog;
import com.gdg.Todak.point.exception.FileException;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.repository.PointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointLogService {

    private final PointLogRepository pointLogRepository;
    private final MemberRepository memberRepository;
    @Value("${file.path}")
    private String uploadFolder;

    public Page<PointLogResponse> getPointLogList(String userId, Pageable pageable) {
        Member member = getMember(userId);

        return pointLogRepository.findAllByMember(member, pageable)
                .map(pointLog -> new PointLogResponse(
                        pointLog.getPointType(),
                        pointLog.getPointStatus(),
                        pointLog.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        pointLog.getPoint()));
    }

    @Transactional
    public void createPointLog(PointLogRequest pointLogRequest) {
        PointLog pointLog = PointLog.builder()
                .member(pointLogRequest.member())
                .point(pointLogRequest.point())
                .pointType(pointLogRequest.pointType())
                .pointStatus(pointLogRequest.pointStatus())
                .build();

        pointLogRepository.save(pointLog);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                savePointLogToServer(pointLogRequest);
            }
        });
    }

    private void savePointLogToServer(PointLogRequest pointLogRequest) {
        String subDirectory = "pointLogs/" + pointLogRequest.member().getUserId();
        Path directoryPath = Paths.get(uploadFolder, subDirectory);
        Path logFilePath = directoryPath.resolve("logs.txt");

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String logEntry = String.format(
                    "[%s] <Status: %s> UserId: %s, Point: %d, Type: %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    pointLogRequest.pointStatus(),
                    pointLogRequest.member().getUserId(),
                    pointLogRequest.point(),
                    pointLogRequest.pointType()
            );

            Files.writeString(logFilePath, logEntry,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new FileException("포인트 로그 업로드를 실패했습니다.");
        }
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
