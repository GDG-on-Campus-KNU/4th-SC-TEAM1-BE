package com.gdg.Todak.tree.business;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.service.PointService;
import com.gdg.Todak.tree.business.dto.TreeEntityDto;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import com.gdg.Todak.tree.domain.GrowthButton;
import com.gdg.Todak.tree.domain.TreeExperiencePolicy;
import com.gdg.Todak.tree.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreeServiceTest {

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private TreeService treeService;

    private Member testMember;
    private TreeEntityDto treeEntityDto;

    @BeforeEach
    void setUp() {
        testMember = Member.of("testId", "testUser", "testNick", "image.jpg", "test");

        treeEntityDto = TreeEntityDto.create(1L,
                TreeExperiencePolicy.INITIAL_LEVEL.getValue(),
                TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue(),
                false,
                testMember);
    }

    @Test
    @DisplayName("나무 생성 성공")
    void getTreeSuccessTest() {
        // given
        given(treeRepository.existsByMember(any(Member.class))).willReturn(false);
        doNothing().when(treeRepository).saveTreeByMember(any(Member.class));

        // when
        treeService.getTree(testMember);

        // then
        verify(treeRepository, times(1)).saveTreeByMember(testMember);
    }

    @Test
    @DisplayName("이미 나무가 존재하는 경우 예외 발생")
    void getTreeFailWhenTreeAlreadyExistsTest() {
        // given
        given(treeRepository.existsByMember(any(Member.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> treeService.getTree(testMember))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("한 멤버당 소유할 수 있는 나무의 수는 한그루 입니다.");

        verify(treeRepository, never()).saveTreeByMember(any(Member.class));
    }

    @Test
    @DisplayName("나무 경험치 획득 성공 - 물 버튼")
    void earnExperienceWithWaterSuccessTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.of(testMember));
        given(treeRepository.findByMember(any(Member.class))).willReturn(treeEntityDto);
        doNothing().when(pointService).consumePointByGrowthButton(any(Member.class), any(GrowthButton.class));
        doNothing().when(treeRepository).update(any(Member.class), any());

        // when
        String result = treeService.earnExperience("testUser", GrowthButton.WATER);

        // then
        assertThat(result).isEqualTo("정상적으로 경험치를 획득하였습니다.");
        verify(pointService, times(1)).consumePointByGrowthButton(testMember, GrowthButton.WATER);
        verify(treeRepository, times(1)).update(eq(testMember), any());
    }

    @Test
    @DisplayName("나무 경험치 획득 성공 - 햇빛 버튼")
    void earnExperienceWithSunSuccessTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.of(testMember));
        given(treeRepository.findByMember(any(Member.class))).willReturn(treeEntityDto);
        doNothing().when(pointService).consumePointByGrowthButton(any(Member.class), any(GrowthButton.class));
        doNothing().when(treeRepository).update(any(Member.class), any());

        // when
        String result = treeService.earnExperience("testUser", GrowthButton.SUN);

        // then
        assertThat(result).isEqualTo("정상적으로 경험치를 획득하였습니다.");
        verify(pointService, times(1)).consumePointByGrowthButton(testMember, GrowthButton.SUN);
        verify(treeRepository, times(1)).update(eq(testMember), any());
    }

    @Test
    @DisplayName("나무 경험치 획득 성공 - 영양분 버튼")
    void earnExperienceWithNutrientSuccessTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.of(testMember));
        given(treeRepository.findByMember(any(Member.class))).willReturn(treeEntityDto);
        doNothing().when(pointService).consumePointByGrowthButton(any(Member.class), any(GrowthButton.class));
        doNothing().when(treeRepository).update(any(Member.class), any());

        // when
        String result = treeService.earnExperience("testUser", GrowthButton.NUTRIENT);

        // then
        assertThat(result).isEqualTo("정상적으로 경험치를 획득하였습니다.");
        verify(pointService, times(1)).consumePointByGrowthButton(testMember, GrowthButton.NUTRIENT);
        verify(treeRepository, times(1)).update(eq(testMember), any());
    }

    @Test
    @DisplayName("최대 성장에 도달한 나무에 경험치 획득 시도 시 예외 발생")
    void earnExperienceFailWhenTreeMaxedTest() {
        // given
        TreeEntityDto maxedTreeDto = TreeEntityDto.create(1L,
                TreeExperiencePolicy.MAX_LEVEL.getValue(),
                TreeExperiencePolicy.LEVEL_FIVE_MAX_EXPERIENCE.getValue(),
                true,
                testMember);

        given(memberRepository.findByUserId(anyString())).willReturn(Optional.of(testMember));
        given(treeRepository.findByMember(any(Member.class))).willReturn(maxedTreeDto);

        // when & then
        assertThatThrownBy(() -> treeService.earnExperience("testUser", GrowthButton.WATER))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("최고 레벨입니다.");

        verify(pointService, never()).consumePointByGrowthButton(any(Member.class), any(GrowthButton.class));
        verify(treeRepository, never()).update(any(Member.class), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 경험치 획득 시도 시 예외 발생")
    void earnExperienceFailWhenUserNotFoundTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> treeService.earnExperience("nonExistingUser", GrowthButton.WATER))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("userId에 해당하는 멤버가 없습니다.");
    }

    @Test
    @DisplayName("트리 정보 조회 성공")
    void getTreeInfoSuccessTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.of(testMember));
        given(treeRepository.findByMember(any(Member.class))).willReturn(treeEntityDto);

        // when
        TreeInfoResponse response = treeService.getMyTreeInfo("testUser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.level()).isEqualTo(TreeExperiencePolicy.INITIAL_LEVEL.getValue());
        assertThat(response.experience()).isEqualTo(TreeExperiencePolicy.INITIAL_EXPERIENCE.getValue());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 트리 정보 조회 시 예외 발생")
    void getTreeInfoFailWhenUserNotFoundTest() {
        // given
        given(memberRepository.findByUserId(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> treeService.getMyTreeInfo("nonExistingUser"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("userId에 해당하는 멤버가 없습니다.");
    }
}
