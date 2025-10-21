package com.openstep.balllinkbe.domain.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${cdn.base-url}")
    private String cdnBaseUrl;

    @Value("${ncp.object-storage.endpoint}")
    private String endpoint;

    @Value("${ncp.object-storage.region}")
    private String region;

    @Value("${ncp.object-storage.bucket}")
    private String bucket;

    @Value("${ncp.object-storage.access-key}")
    private String accessKey;

    @Value("${ncp.object-storage.secret-key}")
    private String secretKey;

    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    @Override
    public String storeFile(Long ownerId, FileMeta.OwnerType ownerType, FileMeta.FileCategory category,
                            String originalName, byte[] content) {
        // 확장자 추출
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = ownerId + "_" + timestamp + "." + ext;

        // 상대경로 결정
        String relativePath = switch (category) {
            case PROFILE -> "profiles/" + ownerId + "/" + fileName;
            case EMBLEM -> "teams/" + ownerId + "/" + fileName;
            case RESULT -> "scrimmages/" + ownerId + "/" + fileName;
            case DOC -> "files/" + ownerId + "/" + fileName;
        };

        // === 업로드 ===
        try (S3Client s3 = buildClient()) {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(relativePath)
                            .contentType(detectContentType(ext))
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패: " + relativePath, e);
        }

        return relativePath;
    }

    @Override
    public String toCdnUrl(String relativePath) {
        // 절대경로 변환 안 함. 그대로 반환
        return relativePath;
    }

    private String detectContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}
