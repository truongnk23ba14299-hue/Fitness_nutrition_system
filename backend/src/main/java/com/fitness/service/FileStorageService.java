package com.fitness.service;

import com.fitness.dto.FileUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file tải lên!", ex);
        }
    }

    public FileUploadResponse storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn tệp tin cần tải lên!");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận tệp hình ảnh (.jpg, .jpeg, .png, .webp, .gif)!");
        }

        // Tạo tên tệp độc nhất tránh ghi đè
        String uniqueFileName = UUID.randomUUID() + extension;
        Path targetLocation = this.uploadDir.resolve(uniqueFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu tệp lên máy chủ: " + ex.getMessage());
        }

        // Đường dẫn tương đối phục vụ ảnh tĩnh
        String fileUrl = "/uploads/" + uniqueFileName;

        return FileUploadResponse.builder()
                .fileName(uniqueFileName)
                .fileUrl(fileUrl)
                .fileType(file.getContentType())
                .size(file.getSize())
                .build();
    }
}