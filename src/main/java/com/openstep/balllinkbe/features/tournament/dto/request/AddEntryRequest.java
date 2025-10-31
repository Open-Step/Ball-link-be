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

@Getter @Setter
public class AddEntryRequest {

    @Schema(description = "홈팀 엔트리 목록")
    private List<PlayerEntry> homeEntries = new ArrayList<>();

    @Schema(description = "어웨이팀 엔트리 목록")
    private List<PlayerEntry> awayEntries = new ArrayList<>();

    public List<TournamentEntry> toEntities(Tournament tournament, Game game) {
        List<TournamentEntry> result = new ArrayList<>();
        if (game.getHomeTeam() != null && homeEntries != null)
            result.addAll(toEntryList(tournament, game.getHomeTeam(), homeEntries));
        if (game.getAwayTeam() != null && awayEntries != null)
            result.addAll(toEntryList(tournament, game.getAwayTeam(), awayEntries));
        return result;
    }

    private List<TournamentEntry> toEntryList(Tournament tournament, Team team, List<PlayerEntry> entries) {
        List<TournamentEntry> list = new ArrayList<>();
        for (PlayerEntry e : entries) {
            list.add(TournamentEntry.builder()
                    .tournament(tournament)
                    .team(team)
                    .player(Player.builder().id(e.getPlayerId()).build())
                    .number(e.getNumber())
                    .position(e.getPosition())
                    .note(e.getNote())
                    .locked(false)
                    .build());
        }
        return list;
    }

    @Getter @Setter
    public static class PlayerEntry {
        private Long playerId;     // 필수: Player.id
        private Short number;      // 등번호
        private Position position; // PG/SG/SF/PF/C
        private String note;
    }
}
