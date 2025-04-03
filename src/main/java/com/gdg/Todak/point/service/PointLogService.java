package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.entity.PointLog;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.repository.PointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointLogService {

    private final PointLogRepository pointLogRepository;
    private final MemberRepository memberRepository;

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
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
