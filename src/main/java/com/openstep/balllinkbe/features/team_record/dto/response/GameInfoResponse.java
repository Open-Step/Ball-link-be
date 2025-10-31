package com.openstep.balllinkbe.features.team_record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 박스스코어 제외 기본 경기 정보 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoResponse {

    @Schema(description = "경기 ID")
    private Long gameId;

    @Schema(description = "대회명")
    private String tournamentName;

    @Schema(description = "경기 예정 일시")
    private LocalDateTime scheduledAt;

    @Schema(description = "경기장 이름")
    private String venueName;

    @Schema(description = "홈팀 ID")
    private Long homeTeamId;

    @Schema(description = "홈팀명")
    private String homeTeamName;

    @Schema(description = "어웨이팀 ID")
    private Long awayTeamId;

    @Schema(description = "어웨이팀명")
    private String awayTeamName;
}
