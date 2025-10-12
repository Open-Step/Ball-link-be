package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자체전 종료/내보내기 응답")
public record EndScrimmageResponse(
        @Schema(description = "생성된 기록지 파일의 CDN URL", example = "https://www.오픈스텝.com/scrimmages/9001/9001_20250830143000.pdf")
        String url,
        @Schema(description = "생성된 기록지 파일의 이름", example = "9001_20250830143000.pdf")
        String fileName
) {}
