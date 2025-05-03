package com.gdg.Todak.tree.repository.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.repository.entity.TreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreeJpaRepository extends JpaRepository<TreeEntity, Long> {

    Optional<TreeEntity> findByMember(Member member);

    boolean existsByMember(Member member);
}
