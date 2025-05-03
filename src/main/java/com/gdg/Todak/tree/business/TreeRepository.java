package com.gdg.Todak.tree.business;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.tree.business.dto.TreeEntityDto;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;

public interface TreeRepository {
    void saveTreeByMember(Member member);

    TreeEntityDto findByMember(Member member);

    void update(Member member, TreeEntityUpdateRequest treeEntityUpdateRequest);

    boolean existsByMember(Member member);
}
