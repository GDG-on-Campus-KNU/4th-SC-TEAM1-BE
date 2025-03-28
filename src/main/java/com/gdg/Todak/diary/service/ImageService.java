package com.gdg.Todak.diary.service;


import com.gdg.Todak.diary.dto.UrlResponse;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",   // JPG 이미지
            "image/png",    // PNG 이미지
            "image/gif",    // GIF 이미지
            "image/bmp",    // BMP 이미지
            "image/webp",   // WEBP 이미지
            "image/svg+xml" // SVG 이미지
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    @Value("${file.path}")
    private String uploadFolder;
    @Value("${image.url}")
    private String imageUrl;

    public UrlResponse uploadImage(MultipartFile file, String storageUUID, String userName) {
        if (file.isEmpty()) throw new BadRequestException("이미지가 비어있습니다.");
        if (file.getSize() > MAX_FILE_SIZE) throw new BadRequestException("파일 크기가 10MB를 초과했습니다.");
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType()))
            throw new BadRequestException("잘못된 형식의 이미지를 업로드하였습니다. (가능한 형식: jpg, png, gif, bmp, webp, svg)");

        String subDirectory = userName + "/" + storageUUID;

        try {
            Path directoryPath = Paths.get(uploadFolder + subDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            throw new FileException("이미지 업로드를 실패하였습니다.");
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destinationFile = new File(uploadFolder + subDirectory + "/" + fileName);

        try {
            file.transferTo(destinationFile);
            return new UrlResponse(imageUrl + subDirectory + "/" + fileName);
        } catch (IOException e) {
            throw new FileException("이미지 업로드를 실패하였습니다.");
        }
    }

    public void deleteImage(String url, String userName) {
        // url example: /backend/images/testUser/1234/Frame.png
        String[] parts = url.split("/");
        if (parts.length != 6) throw new BadRequestException("입력 url 형식이 잘못되었습니다.");
        String storageUUID = parts[4];
        String filename = parts[5];

        File targetFile = new File(uploadFolder + userName + "/" + storageUUID + "/" + filename);
        if (!targetFile.delete()) throw new FileException("이미지 삭제를 실패하였습니다.");
    }

    public void deleteAllImagesInStorageUUID(String userName, String storageUUID) {
        Path directoryPath = Paths.get(uploadFolder + userName + "/" + storageUUID);

        if (!Files.exists(directoryPath)) {
            return;
        }

        File directory = directoryPath.toFile();
        File[] images = directory.listFiles();
        if (images != null) {
            for (File image : images) {
                if (!image.delete()) {
                    throw new FileException("이미지 삭제를 실패하였습니다.");
                }
            }
        }
        if (!directory.delete()) throw new FileException("이미지 삭제를 실패하였습니다.");
    }
}
