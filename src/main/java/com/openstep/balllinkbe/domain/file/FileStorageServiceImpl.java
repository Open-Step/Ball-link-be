package com.openstep.balllinkbe.domain.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    /** 로컬 저장소 루트 경로 (예: /var/www) */
    @Value("${file.storage.root:/var/www}")
    private String storageRoot;

    @Override
    public String storeFile(Long ownerId, FileMeta.OwnerType ownerType, FileMeta.FileCategory category,
                            String originalName, byte[] content) {
        // 확장자 추출
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = ownerId + "_" + timestamp + "." + ext;

        // 상대경로 (profiles / teams)
        String relativePath = switch (category) {
            case PROFILE -> "profiles/" + ownerId + "_" + fileName;
            case EMBLEM -> "teams/" + ownerId + "_" + fileName;
            default -> throw new IllegalArgumentException("지원하지 않는 파일 카테고리입니다: " + category);
        };

        // 실제 저장 경로
        File targetFile = new File(storageRoot, relativePath);
        targetFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(content);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + targetFile.getAbsolutePath(), e);
        }

        // DB에는 상대경로만 저장
        return relativePath;
    }

    @Override
    public String toCdnUrl(String relativePath) {
        // CDN 생략 — 절대경로로 변환하지 않고 그대로 반환
        return relativePath;
    }
}
