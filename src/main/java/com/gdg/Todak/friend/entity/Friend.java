package com.gdg.Todak.friend.entity;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.exception.BadRequestException;
import com.gdg.Todak.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "requester_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member requester;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "accepter_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member accepter;
    @NotNull
    private FriendStatus friendStatus;

    public boolean checkMemberIsNotAccepter(Member member) {
        return !this.accepter.equals(member);
    }

    public boolean checkMemberIsNotRequester(Member member) {
        return !this.requester.equals(member);
    }

    public void acceptFriendRequest() {
        if (!this.friendStatus.equals(FriendStatus.PENDING)) {
            throw new BadRequestException("대기중인 친구 요청이 아닙니다.");
        }
        this.friendStatus = FriendStatus.ACCEPTED;
    }

    public void declinedFriendRequest() {
        if (!this.friendStatus.equals(FriendStatus.PENDING)) {
            throw new BadRequestException("대기중인 친구 요청이 아닙니다.");
        }
        this.friendStatus = FriendStatus.DECLINED;
    }
}
