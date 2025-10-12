package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성공 응답")
public record SuccessResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success
) {
}
