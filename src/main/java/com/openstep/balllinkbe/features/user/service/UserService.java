package com.openstep.balllinkbe.features.user.service;

import com.openstep.balllinkbe.domain.file.FileMeta;
import com.openstep.balllinkbe.domain.file.FileStorageService;
import com.openstep.balllinkbe.domain.file.FileValidator;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.auth.repository.AuthRepository;
import com.openstep.balllinkbe.features.user.dto.response.UserResponse;
import com.openstep.balllinkbe.features.user.repository.UserRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void updateEmail(Long userId, String email) {
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setEmail(email);
        authRepository.save(user);
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // ---- 파일 검증 ----
        validateImageFile(file);

        try {
            String relativePath = fileStorageService.storeFile(
                    userId,
                    FileMeta.OwnerType.USER,
                    FileMeta.FileCategory.PROFILE,
                    file.getOriginalFilename(),
                    file.getBytes()
            );

            user.setProfileImagePath(relativePath);
            userRepository.save(user);

            return fileStorageService.toCdnUrl(relativePath);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /** 이미지 파일 검증 (CustomException 적용) */
    private void validateImageFile(MultipartFile file) {
        FileValidator.validateImageFile(file);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String cdnUrl = user.getProfileImagePath() != null
                ? fileStorageService.toCdnUrl(user.getProfileImagePath())
                : null;

        return new UserResponse(user, cdnUrl);
    }
}
