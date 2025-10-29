package com.openstep.balllinkbe.features.game.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 대회 경기 생성 */
@Getter @Setter
@Schema(description = "대회 경기 생성 요청 DTO")
public class CreateTournamentGameRequest {

    @Schema(description = "홈 팀 ID", example = "1", required = true)
    private Long homeTeamId;

    @Schema(description = "원정 팀 ID", example = "2", required = true)
    private Long awayTeamId;

    @Schema(description = "경기장 이름", example = "강남체육관")
    private String venueName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "경기 예정 시간", example = "2025-11-01T18:00:00", required = true)
    private LocalDateTime scheduledAt;

    // 사용자가 몇 강인지 숫자로 입력 (16 → 16강, 8 → 8강, 4 → 4강, 2 → 결승)
    @Schema(description = "라운드(강) 숫자 입력 (16, 8, 4, 2)", example = "16")
    private Integer round;

    @Schema(description = "브래킷 순서 (선택)", example = "1")
    private Integer bracketOrder;
}
