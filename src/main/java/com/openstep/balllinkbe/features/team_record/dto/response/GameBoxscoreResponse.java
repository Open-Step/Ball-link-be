package com.openstep.balllinkbe.features.team_record.dto.response;

import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerLineProjection;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class GameBoxscoreResponse {
    private Long gameId;
    private Long tournamentId;
    private String tournamentName;
    private LocalDateTime date;
    private String venueName;

    private TeamBox home;
    private TeamBox away;

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class TeamBox {
        private Long teamId;
        private String teamName;
        private Totals totals;
        private List<PlayerLine> players;

        public static TeamBox from(Long teamId, String teamName, GameTeamStat t, List<PlayerLine> players) {
            Totals totals = (t == null) ? new Totals(0,0,0,0,0,0,0) :
                    new Totals(t.getPts(), t.getReb(), t.getAst(), t.getStl(), t.getBlk(), t.getPf(), t.getTov());
            return TeamBox.builder().teamId(teamId).teamName(teamName).totals(totals).players(players).build();
        }
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Totals {
        private int pts, reb, ast, stl, blk, pf, tov;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class PlayerLine {
        private Long playerId;
        private String playerName;
        private Integer backNumber;
        private String position;

        private int pts, reb, ast, stl, blk;
        private int fg2Made, fg2Att, fg3Made, fg3Att, ftMade, ftAtt;
        private int pf, tov;
        private BigDecimal minutes;

        public static PlayerLine from(PlayerLineProjection p) {
            return PlayerLine.builder()
                    .playerId(p.getPlayerId())
                    .playerName(p.getPlayerName())
                    .backNumber(p.getBackNumber())
                    .position(p.getPosition())
                    .pts(nz(p.getPts())).reb(nz(p.getReb())).ast(nz(p.getAst())).stl(nz(p.getStl())).blk(nz(p.getBlk()))
                    .fg2Made(nz(p.getFg2Made())).fg2Att(nz(p.getFg2Att()))
                    .fg3Made(nz(p.getFg3Made())).fg3Att(nz(p.getFg3Att()))
                    .ftMade(nz(p.getFtMade())).ftAtt(nz(p.getFtAtt()))
                    .pf(nz(p.getPf())).tov(nz(p.getTov()))
                    .minutes(p.getMinutes())
                    .build();
        }
        private static int nz(Integer v) { return v == null ? 0 : v; }
    }
}
