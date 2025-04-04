package com.gdg.Todak.point.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
    boolean existsByMember(Member member);

    Optional<Point> findByMember(Member member);
}
