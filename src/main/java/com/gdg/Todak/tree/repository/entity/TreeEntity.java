package com.gdg.Todak.tree.repository.entity;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.business.dto.TreeEntityDto;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;
import com.gdg.Todak.tree.domain.TreeConfig;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int level;
    private int experience;
    private boolean isMaxGrowth;
    @OneToOne
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    public static TreeEntity createByMember(Member member) {
        return TreeEntity.builder()
                .level(TreeConfig.INITIAL_LEVEL.getValue())
                .experience(TreeConfig.INITIAL_EXPERIENCE.getValue())
                .isMaxGrowth(false)
                .member(member)
                .build();
    }

    public void update(TreeEntityUpdateRequest treeEntityUpdateRequest) {
        this.level = treeEntityUpdateRequest.level();
        this.experience = treeEntityUpdateRequest.experience();
        this.isMaxGrowth = treeEntityUpdateRequest.isMaxGrowth();
    }

    public TreeEntityDto toEntityDto() {
        return TreeEntityDto.create(id, level, experience, isMaxGrowth, member);
    }
}
