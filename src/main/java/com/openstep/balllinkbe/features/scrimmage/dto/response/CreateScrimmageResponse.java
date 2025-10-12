package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자체전 생성 응답")
public record CreateScrimmageResponse(
        @Schema(description = "생성된 게임 ID", example = "9001")
        Long gameId
) {
}
