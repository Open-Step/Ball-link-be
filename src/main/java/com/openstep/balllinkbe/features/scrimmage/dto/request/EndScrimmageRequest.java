package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자체전 종료/내보내기 요청")
public record EndScrimmageRequest(
        @Schema(description = "내보낼 기록지의 형식", example = "PDF")
        String exportFormat
) {}