package com.openstep.balllinkbe.domain.file;

public interface FileStorageService {
    /**
     * 파일 업로드 후 상대경로를 반환한다.
     * ex) profiles/42/42_20250926131500.jpg
     */
    String storeFile(Long ownerId, FileMeta.OwnerType ownerType, FileMeta.FileCategory category, String originalName, byte[] content);

    /**
     * 상대경로를 받아 CDN 절대경로로 변환한다.
     * ex) https://www.오픈스텝.com/profiles/42/42_20250926131500.jpg
     */
    String toCdnUrl(String relativePath);
}
