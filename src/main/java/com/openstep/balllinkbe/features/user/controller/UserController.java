package com.openstep.balllinkbe.features.user.controller;

import com.openstep.balllinkbe.domain.file.FileValidator;
import com.openstep.balllinkbe.features.user.dto.request.UpdateEmailRequest;
import com.openstep.balllinkbe.features.user.dto.response.ProfileImageResponse;
import com.openstep.balllinkbe.features.user.dto.response.UserResponse;
import com.openstep.balllinkbe.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "user-controller", description = "사용자 정보 관리 API")
public class UserController {

    private final UserService userService;

    @PutMapping("/email")
    @Operation(summary = "이메일 변경", description = "현재 로그인한 사용자의 이메일 주소를 수정합니다.")
    public ResponseEntity<?> updateEmail(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateEmailRequest request
    ) {
        userService.updateEmail(userId, request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 업로드/변경합니다.")
    public ResponseEntity<ProfileImageResponse> updateProfileImage(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        // 5MB + 확장자 검증
        FileValidator.validateImageFile(file);
        String url = userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(new ProfileImageResponse(url));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getMyInfo(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }
}
