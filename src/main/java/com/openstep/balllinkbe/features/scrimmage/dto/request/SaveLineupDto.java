package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "자체전 라인업 저장 요청")
public record SaveLineupDto(
        @Schema(description = "홈팀 라인업")
        List<String> home,
        @Schema(description = "어웨이팀 라인업")
        List<String> away
) {
}
