package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.entity.PointLog;
import com.gdg.Todak.point.repository.PointLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "file.path=test-uploads/"
})
class PointLogServiceTest {

    @Autowired
    private PointLogService pointLogService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberRoleRepository memberRoleRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Value("${file.path}")
    private String uploadFolder;

    private Member testMember;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        Path testPath = Paths.get(uploadFolder);
        if (!Files.exists(testPath)) {
            Files.createDirectories(testPath);
        }

        testMember = memberRepository.save(Member.builder()
                .userId("testUser")
                .imageUrl("test.com")
                .nickname("testUser")
                .password("testPW")
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();

        memberRoleRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        pointLogRepository.deleteAllInBatch();

        File testDir = new File(uploadFolder);
        if (testDir.exists() && testDir.isDirectory()) {
            deleteFilesRecursively(testDir);
            testDir.delete();
        }
    }

    private void deleteFilesRecursively(File file) {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                deleteFilesRecursively(subFile);
            }
        }
        file.delete();
    }

    @Test
    @DisplayName("포인트 로그 생성 및 로그 파일 저장 성공")
    void createPointLogAndWriteToFile() throws Exception {
        // given
        PointLogRequest request = new PointLogRequest(testMember, 100, PointType.DIARY, PointStatus.EARNED, LocalDateTime.now());

        // when
        pointLogService.createPointLog(request);

        // then
        String logFilePath = uploadFolder + "pointLogs/" + testMember.getUserId() + "/logs.txt";
        Path path = Paths.get(logFilePath);
        assertThat(Files.exists(path)).isTrue();

        String content = Files.readString(path);
        assertThat(content).contains("UserId: testUser", "Point: 100", "Type: DIARY");
    }

    @Test
    @DisplayName("포인트 로그 조회 성공")
    void getPointLogListSuccess() {
        // given
        pointLogRepository.save(PointLog.builder().member(testMember).point(100).pointType(PointType.DIARY).pointStatus(PointStatus.EARNED).build());

        // when
        Page<PointLogResponse> pointLogs = pointLogService.getPointLogList(testMember.getUserId(), Pageable.unpaged());

        // then
        assertThat(pointLogs).isNotEmpty();
        assertThat(pointLogs.getContent().get(0).point()).isEqualTo(100);
        assertThat(pointLogs.getContent().get(0).pointType()).isEqualTo(PointType.DIARY);
    }
}
