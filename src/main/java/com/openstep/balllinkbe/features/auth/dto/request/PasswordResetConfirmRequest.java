package com.openstep.balllinkbe.features.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordResetConfirmRequest {

    @Schema(description = "비밀번호 재설정 토큰", example = "d14c53e3-8f9d-4e92-a7e7-2bbd6e558e45")
    private String token;

    @Schema(description = "새 비밀번호", example = "newStrongPassword123!")
    private String newPassword;
}
