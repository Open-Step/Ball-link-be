package com.openstep.balllinkbe.domain.file;

import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private FileValidator() {}

    public static void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"))) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    public static void validatePdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
