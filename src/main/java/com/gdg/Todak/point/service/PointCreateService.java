package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.exception.ConflictException;
import com.gdg.Todak.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointCreateService {

    private final PointRepository pointRepository;

    @Transactional
    public void createPoint(Member member) {
        if (pointRepository.existsByMember(member)) {
            throw new ConflictException("이미 해당 멤버의 point 객체가 존재합니다.");
        }

        Point point = Point.builder().member(member).build();
        pointRepository.save(point);
    }
}
