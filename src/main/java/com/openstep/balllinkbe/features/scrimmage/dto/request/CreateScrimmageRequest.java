package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "자체전 생성 요청")
public record CreateScrimmageRequest(
        @Schema(description = "경기 예정 일시", example = "2025-08-30T13:00:00Z")
        LocalDateTime scheduledAt,
        @Schema(description = "경기장 ID", example = "3")
        Long venueId,
        @Schema(description = "상대팀 이름", example = "연습팀")
        String opponentName,
        @Schema(description = "메모", example = "5쿼터 진행")
        String note
) {
}
