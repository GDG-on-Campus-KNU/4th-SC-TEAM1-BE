package com.gdg.Todak.point.entity;

import com.gdg.Todak.common.domain.BaseEntity;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;
    @NotNull
    private int point;
    @NotNull
    @Enumerated(EnumType.STRING)
    private PointType pointType;
    @NotNull
    @Enumerated(EnumType.STRING)
    private PointStatus pointStatus;
}
