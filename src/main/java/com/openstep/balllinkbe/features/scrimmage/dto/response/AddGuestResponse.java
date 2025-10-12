package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자체전 게스트 추가 응답")
public record AddGuestResponse(
        @Schema(description = "추가된 게스트 ID", example = "123")
        Long id,
        @Schema(description = "추가된 게스트 이름", example = "게스트")
        String name
) {
}
