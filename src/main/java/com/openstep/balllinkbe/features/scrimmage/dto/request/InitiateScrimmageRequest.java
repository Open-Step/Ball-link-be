package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자체전 생성 + 엔트리 등록 요청 DTO")
public class InitiateScrimmageRequest {

    @Schema(description = "홈 팀 ID", example = "1")
    private Long homeTeamId;

    @Schema(description = "원정 팀 ID", example = "2")
    private Long awayTeamId;

    @Schema(description = "홈팀 선수 목록")
    private List<AddEntryRequest.EntryPlayerDto> homePlayers;

    @Schema(description = "원정팀 선수 목록")
    private List<AddEntryRequest.EntryPlayerDto> awayPlayers;

    @Schema(description = "세션 토큰 발급 여부", example = "true")
    private boolean createSession = true;
}
