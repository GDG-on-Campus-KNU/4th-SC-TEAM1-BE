package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByMemberAndCreatedAtBetween(Member member, Instant startPoint, Instant endPoint);

    boolean existsByMemberAndCreatedAtBetween(Member member, Instant startOfDay, Instant endOfDay);

    Optional<Diary> findByMemberAndContent(Member writer, String content);
}
