package com.openstep.balllinkbe.features.tournament.dto.request;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.enums.Position;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 대회 경기 원샷 생성 요청 DTO
 * 홈/어웨이 팀의 엔트리를 한 번에 등록합니다.
 */
@Getter
@Setter
public class InitiateTournamentRequest {

    @Schema(description = "홈팀 엔트리 목록", example = """
        [
          {"playerId": 11, "number": 7, "position": "PG", "note": "주장"},
          {"playerId": 12, "number": 9, "position": "SF"}
        ]
        """)
    private List<PlayerEntry> homeEntries = new ArrayList<>();

    @Schema(description = "어웨이팀 엔트리 목록", example = """
        [
          {"playerId": 21, "number": 4, "position": "PF"},
          {"playerId": 22, "number": 6, "position": "C"}
        ]
        """)
    private List<PlayerEntry> awayEntries = new ArrayList<>();

    /**
     * DTO → TournamentEntry 엔티티 변환
     */
    public List<TournamentEntry> toEntities(Tournament tournament, Game game) {
        List<TournamentEntry> result = new ArrayList<>();

        if (game.getHomeTeam() != null && homeEntries != null) {
            result.addAll(toEntryList(tournament, game.getHomeTeam(), homeEntries));
        }
        if (game.getAwayTeam() != null && awayEntries != null) {
            result.addAll(toEntryList(tournament, game.getAwayTeam(), awayEntries));
        }

        return result;
    }

    private List<TournamentEntry> toEntryList(Tournament tournament, Team team, List<PlayerEntry> entries) {
        List<TournamentEntry> list = new ArrayList<>();

        for (PlayerEntry e : entries) {
            list.add(
                    TournamentEntry.builder()
                            .tournament(tournament)
                            .team(team)
                            .player(Player.builder().id(e.getPlayerId()).build())
                            .number(e.getNumber())
                            .position(e.getPosition())
                            .note(e.getNote())
                            .locked(false)
                            .build()
            );
        }

        return list;
    }

    /**
     * 개별 선수 엔트리 정보
     */
    @Getter
    @Setter
    public static class PlayerEntry {

        @Schema(description = "선수 ID (Player ID)", example = "11")
        private Long playerId;

        @Schema(description = "등번호", example = "7")
        private Short number;

        @Schema(description = "포지션 (PG, SG, SF, PF, C)", example = "PG")
        private Position position;

        @Schema(description = "비고", example = "주장 / 복귀 예정")
        private String note;
    }
}
