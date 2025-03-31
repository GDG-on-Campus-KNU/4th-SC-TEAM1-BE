package com.gdg.Todak.diary.repository;

import com.gdg.Todak.diary.entity.Comment;
import com.gdg.Todak.diary.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByDiary(Diary diary, Pageable pageable);
}
