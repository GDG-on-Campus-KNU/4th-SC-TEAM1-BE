package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
