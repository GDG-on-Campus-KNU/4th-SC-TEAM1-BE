package com.gdg.Todak.member.service;

import com.gdg.Todak.member.controller.dto.LoginForm;
import com.gdg.Todak.member.controller.request.ProfileRequest;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.MemberRole;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.exception.BadRequestException;
import com.gdg.Todak.member.exception.FileException;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.*;
import com.gdg.Todak.member.service.response.*;
import com.gdg.Todak.member.util.JwtProvider;
import com.gdg.Todak.member.util.PasswordEncoder;
import com.gdg.Todak.point.service.PointService;
import com.gdg.Todak.tree.business.TreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final PointService pointService;
    private final TreeService treeService;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",   // JPG 이미지
            "image/png",    // PNG 이미지
            "image/gif",    // GIF 이미지
            "image/bmp",    // BMP 이미지
            "image/webp",   // WEBP 이미지
            "image/svg+xml" // SVG 이미지
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${DEFAULT_PROFILE_IMAGE_URL}")
    private String defaultProfileImageUrl;
    @Value("${file.path}")
    private String uploadFolder;
    @Value("${image.url}")
    private String imageUrl;

    public CheckUserIdServiceResponse checkUserId(CheckUserIdServiceRequest serviceRequest) {
        Optional<Member> findMember = memberRepository.findByUserId(serviceRequest.getUserId());
        return CheckUserIdServiceResponse.of(findMember.isPresent());
    }

    @Transactional
    public MemberResponse signup(SignupServiceRequest request) {

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String salt = PasswordEncoder.getSalt();

        String encodedPassword = PasswordEncoder.getEncodedPassword(salt, request.getPassword());

        Member member = memberRepository.save(
                Member.of(request.getUserId(), encodedPassword, request.getNickname(), defaultProfileImageUrl, salt));

        MemberRole role = MemberRole.of(Role.USER, member);
        member.addRole(role);

        memberRoleRepository.save(role);

        pointService.createPoint(member);

        treeService.getTree(member);

        return MemberResponse.of(member.getUserId());
    }

    @Transactional
    public LoginResponse login(LoginServiceRequest request) {

        Member member = findMember(request.getUserId());

        checkPassword(request.getPassword(), member);

        Set<Role> roles = member.getRoles();

        Map<String, Object> claims = jwtProvider.createClaims(member, roles);

        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(refreshToken, member);

        pointService.earnAttendancePointPerDay(member);

        return LoginResponse.of(member.getUserId(), member.getNickname(), accessToken, refreshToken);
    }

    public LogoutResponse logout(AuthenticateUser user, LogoutServiceRequest serviceRequest) {
        if (user != null) {
            redisTemplate.delete(serviceRequest.getRefreshToken());
        }

        String message = "성공적으로 로그아웃 되었습니다.";

        return LogoutResponse.of(message);
    }

    public MeResponse me(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        return MeResponse.from(member);
    }

    @Transactional
    public MeResponse editMemberNickname(AuthenticateUser user, EditMemberNicknameServiceRequest serviceRequest) {
        Member member = findMember(user.getUserId());
        member.setNickname(serviceRequest.getNickname());
        return MeResponse.of(member.getUserId(), member.getNickname(), member.getImageUrl());
    }

    @Transactional
    public MeResponse editMemberProfileImage(AuthenticateUser user, ProfileRequest serviceRequest) {
        MultipartFile file = serviceRequest.file();
        if (file.isEmpty()) throw new BadRequestException("이미지가 비어있습니다.");
        if (file.getSize() > MAX_FILE_SIZE) throw new BadRequestException("파일 크기가 10MB를 초과했습니다.");
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType()))
            throw new BadRequestException("잘못된 형식의 이미지를 업로드하였습니다. (가능한 형식: jpg, png, gif, bmp, webp, svg)");

        String subDirectory = user.getUserId() + "/profile_image";
        try {
            Path directoryPath = Paths.get(uploadFolder + subDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            throw new FileException("이미지 업로드를 실패하였습니다.");
        }

        String uploadDestination = uploadFolder + subDirectory + "/" + file.getOriginalFilename();

        File destinationFile = new File(uploadDestination);

        try {
            Member member = findMember(user.getUserId());
            if (!member.getImageUrl().equals(defaultProfileImageUrl)) {
                deleteAllImagesInFolder(uploadFolder + subDirectory);
            }
            file.transferTo(destinationFile);
            member.updateImageUrl(imageUrl + subDirectory + "/" + file.getOriginalFilename());

            return MeResponse.of(member.getUserId(), member.getNickname(), member.getImageUrl());
        } catch (IOException e) {
            throw new FileException("프로필 이미지 저장 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public String deleteMemberProfileImage(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        deleteAllImagesInFolder(uploadFolder + user.getUserId() + "/profile_image");
        member.updateImageUrl(defaultProfileImageUrl);
        return "프로필 이미지가 삭제되고 기본 이미지로 변경되었습니다.";
    }

    public void deleteAllImagesInFolder(String folderPath) {
        Path dirPath = Paths.get(folderPath);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new FileException("유효하지 않은 폴더 경로입니다: " + folderPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path filePath : stream) {
                if (Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                }
            }
        } catch (IOException e) {
            throw new FileException("이미지 파일 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public String changePassword(AuthenticateUser user, ChangePasswordServiceRequest request) {
        Member member = findMember(user.getUserId());

        checkPassword(request.getOldPassword(), member);

        if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String salt = PasswordEncoder.getSalt();

        String encodedPassword = PasswordEncoder.getEncodedPassword(salt, request.getNewPassword());

        member.setPassword(encodedPassword);

        return "비밀번호가 변경되었습니다.";
    }

    @Transactional
    public String deleteMember(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        memberRepository.delete(member);
        return "회원이 삭제되었습니다.";
    }

    public String adminLogin(LoginForm request) {

        Optional<Member> findMemberOptional = memberRepository.findByUserId(request.getLoginId());
        if (findMemberOptional.isEmpty()) {
            return null;
        }

        Member member = findMemberOptional.get();
        String encodedPassword = PasswordEncoder.getEncodedPassword(member.getSalt(),
                request.getPassword());
        if (!encodedPassword.equals(member.getPassword())) {
            return null;
        }

        Set<Role> roles = member.getRoles();
        if (!roles.contains(Role.ADMIN)) {
            return null;
        }

        return member.getUserId();
    }

    @Transactional
    public String enableAiComment(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        member.enableAiComment();
        return "AI 댓글 기능이 활성화되었습니다.";
    }

    @Transactional
    public String disableAiComment(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        member.disableAiComment();
        return "AI 댓글 기능이 비활성화되었습니다.";
    }

    private Member findMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("멤버가 존재하지 않습니다."));
    }

    private static void checkPassword(String password, Member member) {
        String encodedPassword = PasswordEncoder.getEncodedPassword(member.getSalt(),
                password);
        if (!encodedPassword.equals(member.getPassword())) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
        }
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        redisTemplate.opsForValue().set(refreshToken, member.getId().toString(), 14, TimeUnit.DAYS);
    }
}
