package com.gdg.Todak.friend.repository;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByRequesterAndAccepter(Member requester, Member accepter);

    List<Friend> findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus
            (String accepterName, FriendStatus friendStatus1, String requesterName, FriendStatus friendStatus2);

    @Query("SELECT COUNT(f) FROM Friend f WHERE f.requester = :member AND f.friendStatus IN :statuses")
    long countByRequesterAndStatusIn(@Param("member") Member member, @Param("statuses") List<FriendStatus> statuses);

    @Query("SELECT COUNT(f) FROM Friend f WHERE f.accepter = :member AND f.friendStatus IN :statuses")
    long countByAccepterAndStatusIn(@Param("member") Member member, @Param("statuses") List<FriendStatus> statuses);

    Optional<Friend> findByRequesterAndAccepter(Member requester, Member accepter);
}
