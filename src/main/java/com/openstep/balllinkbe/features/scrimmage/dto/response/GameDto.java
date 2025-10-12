package com.openstep.balllinkbe.features.scrimmage.dto.response;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "자체전 정보")
public record GameDto(
        @Schema(description = "게임 ID", example = "9001")
        Long gameId,
        @Schema(description = "경기 상태", example = "SCHEDULED")
        String state,
        @Schema(description = "경기 예정 일시", example = "2025-08-30T13:00:00Z")
        LocalDateTime scheduledAt,
        @Schema(description = "홈팀 정보")
        TeamDto home,
        @Schema(description = "어웨이팀 정보")
        TeamDto away,
        @Schema(description = "경기 이벤트 목록")
        List<GameEvent> events
){

    public static GameDto of(Game game, List<GameEvent> events){
        return GameDto.builder()
                .gameId(game.getId())
                .state(game.getState().toString())
                .scheduledAt(game.getScheduledAt())
                .home(TeamDto.from(game.getHomeTeam()))
                .away(TeamDto.from(game.getAwayTeam()))
                .events(events)
                .build();
    }

    public static GameDto from(Game game) {
        return GameDto.of(game, null);
    }
}