package com.openstep.balllinkbe.features.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequest {

    @Schema(description = "현재 비밀번호", example = "oldPassword123!")
    private String oldPassword;

    @Schema(description = "새 비밀번호", example = "newSecurePassword123!")
    private String newPassword;
}
