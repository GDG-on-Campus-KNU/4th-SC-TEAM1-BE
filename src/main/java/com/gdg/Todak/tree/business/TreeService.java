package com.gdg.Todak.tree.business;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.service.PointService;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import com.gdg.Todak.tree.domain.GrowthButton;
import com.gdg.Todak.tree.domain.Tree;
import com.gdg.Todak.tree.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreeService {

    private final TreeRepository treeRepository;
    private final MemberRepository memberRepository;
    private final PointService pointService;

    @Transactional
    public void getTree(Member member) {
        if (treeRepository.existsByMember(member)) {
            throw new BadRequestException("한 멤버당 소유할 수 있는 나무의 수는 한그루 입니다.");
        }

        treeRepository.saveTreeByMember(member);
    }

    @Transactional
    public String earnExperience(String userId, GrowthButton growthButton) {
        Member member = getMember(userId);

        Tree tree = treeRepository.findByMember(member).toDomain();

        if (tree.isMaxGrowth()) {
            throw new BadRequestException("최고 레벨입니다.");
        }

        pointService.consumePointByGrowthButton(member, growthButton);
        tree.earnExperience(growthButton);

        treeRepository.update(member, tree.toTreeEntityUpdateRequest());

        return "정상적으로 경험치를 획득하였습니다.";
    }

    public TreeInfoResponse getTreeInfo(String userId) {
        Member member = getMember(userId);

        Tree tree = treeRepository.findByMember(member).toDomain();

        return tree.toTreeInfoResponse();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
