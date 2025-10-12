package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "자체전 게스트 추가 요청")
public record AddGuestRequest(
        @Schema(description = "게스트 이름", example = "게스트")
        @NotBlank(message = "게스트 이름은 필수입니다.")
        String name
) {
}
