package com.gdg.Todak.tree.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.tree.business.dto.TreeEntityDto;
import com.gdg.Todak.tree.business.dto.TreeEntityUpdateRequest;
import com.gdg.Todak.tree.domain.TreeExperiencePolicy;
import com.gdg.Todak.tree.exception.NotFoundException;
import com.gdg.Todak.tree.repository.entity.TreeEntity;
import com.gdg.Todak.tree.repository.repository.TreeJpaRepository;
import com.gdg.Todak.tree.repository.repository.TreeRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TreeRepositoryImpl.class)
class TreeRepositoryImplTest {

    @Autowired
    private TreeRepositoryImpl treeRepository;

    @Autowired
    private TreeJpaRepository treeJpaRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("testUser", "test@email.com", "password", "010-1234-5678", "test"));
    }

    @DisplayName("회원에게 트리 생성 성공")
    @Test
    void saveTreeByMemberSuccessTest() {
        // when
        treeRepository.saveTreeByMember(member);

        // then
        boolean exists = treeJpaRepository.existsByMember(member);
        assertThat(exists).isTrue();

        TreeEntity treeEntity = treeJpaRepository.findByMember(member).orElseThrow();
        assertThat(treeEntity.getLevel()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(treeEntity.getExperience()).isEqualTo(TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue());
        assertThat(treeEntity.isMaxGrowth()).isFalse();
    }

    @DisplayName("회원으로 트리 조회 성공")
    @Test
    void findByMemberSuccessTest() {
        // given
        treeRepository.saveTreeByMember(member);

        // when
        TreeEntityDto treeEntityDto = treeRepository.findByMember(member);

        // then
        assertThat(treeEntityDto).isNotNull();
        assertThat(treeEntityDto.level()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(treeEntityDto.experience()).isEqualTo(TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue());
        assertThat(treeEntityDto.isMaxGrowth()).isFalse();
        assertThat(treeEntityDto.member().getId()).isEqualTo(member.getId());
    }

    @DisplayName("트리가 존재하지 않는 회원의 트리 조회 시 예외 발생")
    @Test
    void findByMemberNotFoundExceptionTest() {
        // given
        Member member = new Member("nonExistingUser", "non@email.com", "pwd", "010-0000-0000", "test");
        Member nonExistingMember = memberRepository.save(member);

        // when & then
        assertThatThrownBy(() -> treeRepository.findByMember(nonExistingMember))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("member의 tree가 없습니다.");
    }

    @DisplayName("트리 업데이트 성공")
    @Test
    void updateTreeSuccessTest() {
        // given
        treeRepository.saveTreeByMember(member);
        TreeEntityUpdateRequest updateRequest = TreeEntityUpdateRequest.create(3, 150, false);

        // when
        treeRepository.update(member, updateRequest);

        // then
        TreeEntity updatedTree = treeJpaRepository.findByMember(member).orElseThrow();
        assertThat(updatedTree.getLevel()).isEqualTo(3);
        assertThat(updatedTree.getExperience()).isEqualTo(150);
        assertThat(updatedTree.isMaxGrowth()).isFalse();
    }

    @DisplayName("회원의 최대 성장 트리로 업데이트 성공")
    @Test
    void updateTreeToMaxGrowthTest() {
        // given
        treeRepository.saveTreeByMember(member);
        TreeEntityUpdateRequest maxGrowthRequest = TreeEntityUpdateRequest.create(
                TreeExperiencePolicy.MAX_LEVEL.getValue(),
                TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue(),
                true
        );

        // when
        treeRepository.update(member, maxGrowthRequest);

        // then
        TreeEntity updatedTree = treeJpaRepository.findByMember(member).orElseThrow();
        assertThat(updatedTree.getLevel()).isEqualTo(TreeExperiencePolicy.MAX_LEVEL.getValue());
        assertThat(updatedTree.getExperience()).isEqualTo(TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue());
        assertThat(updatedTree.isMaxGrowth()).isTrue();
    }

    @DisplayName("회원의 트리 존재 여부 확인 - 트리가 존재하는 경우")
    @Test
    void existsByMemberWhenTreeExistsTest() {
        // given
        treeRepository.saveTreeByMember(member);

        // when
        boolean exists = treeRepository.existsByMember(member);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("회원의 트리 존재 여부 확인 - 트리가 존재하지 않는 경우")
    @Test
    void existsByMemberWhenTreeDoesNotExistTest() {
        // given
        Member memberWithoutTree = new Member("noTreeUser", "noTree@email.com", "pwd", "010-9999-9999", "noTreeNick");
        memberWithoutTree = memberRepository.save(memberWithoutTree);

        // when
        boolean exists = treeRepository.existsByMember(memberWithoutTree);

        // then
        assertThat(exists).isFalse();
    }
}
