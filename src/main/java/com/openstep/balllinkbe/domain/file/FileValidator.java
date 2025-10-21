package com.openstep.balllinkbe.domain.file;

import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private FileValidator() {}

    /** 이미지 파일 검증 (형식 + 크기) */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"))) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    public static void validatePdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
