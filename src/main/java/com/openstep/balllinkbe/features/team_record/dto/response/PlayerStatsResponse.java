package com.openstep.balllinkbe.features.team_record.dto.response;

import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerAggregateProjection;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class PlayerStatsResponse {
    private String rankBy;
    private List<Item> items;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Item {
        private int rank;
        private Long playerId;
        private String playerName;
        private Integer backNumber;
        private int games;
        private Totals totals;
        private Averages perGame;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Totals {
        private int pts, reb, ast, stl, blk;
        private int fg2Made, fg2Att, fg3Made, fg3Att, ftMade, ftAtt;
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Averages {
        private double pts, reb, ast, stl, blk;
        private double fg2Pct, fg3Pct, ftPct;
    }

    private static double avg(int v, int g) {
        if (g <= 0) return 0.0;
        return BigDecimal.valueOf((double) v / g).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    private static double pct(int made, int att) {
        if (att <= 0) return 0.0;
        return BigDecimal.valueOf((double) made / att * 100).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public static PlayerStatsResponse fromAggregates(List<PlayerAggregateProjection> rows, String rankBy) {
        int[] idx = {1};
        List<Item> items = rows.stream().map(r -> {
            int g = r.getGames() == null ? 0 : r.getGames();
            Totals totals = Totals.builder()
                    .pts(nz(r.getPts())).reb(nz(r.getReb())).ast(nz(r.getAst())).stl(nz(r.getStl())).blk(nz(r.getBlk()))
                    .fg2Made(nz(r.getFg2Made())).fg2Att(nz(r.getFg2Att()))
                    .fg3Made(nz(r.getFg3Made())).fg3Att(nz(r.getFg3Att()))
                    .ftMade(nz(r.getFtMade())).ftAtt(nz(r.getFtAtt()))
                    .build();
            Averages per = Averages.builder()
                    .pts(avg(totals.getPts(), g)).reb(avg(totals.getReb(), g)).ast(avg(totals.getAst(), g))
                    .stl(avg(totals.getStl(), g)).blk(avg(totals.getBlk(), g))
                    .fg2Pct(pct(totals.getFg2Made(), totals.getFg2Att()))
                    .fg3Pct(pct(totals.getFg3Made(), totals.getFg3Att()))
                    .ftPct(pct(totals.getFtMade(), totals.getFtAtt()))
                    .build();
            return Item.builder()
                    .rank(idx[0]++)
                    .playerId(r.getPlayerId())
                    .playerName(r.getPlayerName())
                    .backNumber(r.getBackNumber())
                    .games(g)
                    .totals(totals)
                    .perGame(per)
                    .build();
        }).toList();
        return PlayerStatsResponse.builder().rankBy(rankBy).items(items).build();
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }
}
