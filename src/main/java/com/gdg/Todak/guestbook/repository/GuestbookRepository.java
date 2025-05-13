package com.gdg.Todak.guestbook.repository;

import com.gdg.Todak.guestbook.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

    @Query("SELECT g FROM Guestbook g WHERE g.receiver.userId = :receiverUserId AND g.createdAt < g.expiresAt")
    List<Guestbook> findValidGuestbooksByReceiverUserId(@Param("receiverUserId") String receiverUserId);

    @Modifying
    @Query("DELETE FROM Guestbook g WHERE g.expiresAt < :instant")
    void deleteAllExpiredGuestbooks(@Param("instant") Instant instant);
}
