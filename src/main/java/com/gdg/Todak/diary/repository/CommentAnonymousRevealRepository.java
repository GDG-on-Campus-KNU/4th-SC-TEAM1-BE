package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.CommentAnonymousReveal;
import com.gdg.Todak.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentAnonymousRevealRepository extends JpaRepository<CommentAnonymousReveal, Long> {
    boolean existsByMemberAndComment(Member member, Comment comment);
}
