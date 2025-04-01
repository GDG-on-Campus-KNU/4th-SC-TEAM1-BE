package com.gdg.Todak.diary.service;

import com.gdg.Todak.diary.dto.UrlResponse;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.FileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "file.path=test-uploads/",
        "image.url=/backend/images/"
})
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Value("${file.path}")
    private String uploadFolder;

    @Value("${image.url}")
    private String imageUrl;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        Path testPath = Paths.get(uploadFolder);
        if (!Files.exists(testPath)) {
            Files.createDirectories(testPath);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();

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
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImageSuccessfully() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test data".getBytes()
        );
        String storageUUID = "1234";
        String userName = "testUser";

        // when
        UrlResponse response = imageService.uploadImage(file, storageUUID, userName);

        // then
        assertThat(response).isNotNull();
        assertThat(response.url()).startsWith(imageUrl + userName + "/" + storageUUID);
    }

    @Test
    @DisplayName("잘못된 형식의 이미지 업로드 시 예외 발생")
    void uploadInvalidImageFormat() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "invalid file".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> imageService.uploadImage(file, "1234", "testUser"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("잘못된 형식의 이미지를 업로드하였습니다");
    }

    @Test
    @DisplayName("이미지 삭제 성공")
    void deleteImageSuccessfully() throws IOException {
        // given
        String userName = "testUser";
        String storageUUID = "1234";
        String fileName = "test.jpg";

        Path directoryPath = Paths.get(uploadFolder + userName + "/" + storageUUID);
        Files.createDirectories(directoryPath);
        Path filePath = directoryPath.resolve(fileName);
        Files.createFile(filePath);

        String fileUrl = imageUrl + userName + "/" + storageUUID + "/" + fileName;

        // when
        imageService.deleteImage(fileUrl, userName);

        // then
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 이미지 삭제 시 예외 발생")
    void deleteNonExistentImage() {
        // given
        String fileUrl = "/backend/images/testUser/1234/nonexistent.jpg";

        // when & then
        assertThatThrownBy(() -> imageService.deleteImage(fileUrl, "testUser"))
                .isInstanceOf(FileException.class)
                .hasMessageContaining("이미지 삭제를 실패하였습니다");
    }

    @Test
    @DisplayName("폴더 내 모든 이미지 삭제 성공")
    void deleteAllImagesInStorageUUIDSuccessfully() throws IOException {
        // given
        String userName = "testUser";
        String storageUUID = "1234";
        Path directoryPath = Paths.get(uploadFolder + userName + "/" + storageUUID);
        Files.createDirectories(directoryPath);
        Files.createFile(directoryPath.resolve("test1.jpg"));
        Files.createFile(directoryPath.resolve("test2.jpg"));

        // when
        imageService.deleteAllImagesInStorageUUID(userName, storageUUID);

        // then
        assertThat(Files.exists(directoryPath)).isFalse();
    }
}
