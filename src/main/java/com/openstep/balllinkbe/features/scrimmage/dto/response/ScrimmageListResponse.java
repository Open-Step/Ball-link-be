package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "자체전 목록 조회 응답")
public record ScrimmageListResponse(
        @Schema(description = "페이지 번호", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 항목 수", example = "1")
        int total,
        @Schema(description = "자체전 목록")
        List<GameDto> items
) {
}
