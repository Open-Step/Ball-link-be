package com.openstep.balllinkbe.features.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordResetRequest {

    @Schema(description = "비밀번호 재설정할 계정의 이메일", example = "user@example.com")
    private String email;
}
