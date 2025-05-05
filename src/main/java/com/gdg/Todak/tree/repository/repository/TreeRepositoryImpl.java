package com.gdg.Todak.tree.repository.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.business.TreeRepository;
import com.gdg.Todak.tree.business.dto.TreeEntityDto;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;
import com.gdg.Todak.tree.exception.NotFoundException;
import com.gdg.Todak.tree.repository.entity.TreeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TreeRepositoryImpl implements TreeRepository {

    private final TreeJpaRepository treeJpaRepository;

    @Override
    public void saveTreeByMember(Member member) {
        TreeEntity treeEntity = TreeEntity.createByMember(member);
        treeJpaRepository.save(treeEntity);
    }

    @Override
    public TreeEntityDto findByMember(Member member) {
        TreeEntity treeEntity = treeJpaRepository.findByMember(member)
                .orElseThrow(() -> new NotFoundException("member의 tree가 없습니다."));

        return treeEntity.toEntityDto();
    }

    @Override
    public void update(Member member, TreeEntityUpdateRequest treeEntityUpdateRequest) {
        TreeEntity treeEntity = treeJpaRepository.findByMember(member)
                .orElseThrow(() -> new NotFoundException("member의 tree가 없습니다."));

        treeEntity.update(treeEntityUpdateRequest);
    }

    @Override
    public boolean existsByMember(Member member) {
        return treeJpaRepository.existsByMember(member);
    }
}
