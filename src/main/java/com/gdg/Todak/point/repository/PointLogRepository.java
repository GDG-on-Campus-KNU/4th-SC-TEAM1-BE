package com.gdg.Todak.point.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.entity.PointLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    Page<PointLog> findAllByMember(Member member, Pageable pageable);

    boolean existsByCreatedAtBetweenAndMemberAndPointTypeIn(Instant start, Instant end, Member member, List<PointType> pointTypes);

}
