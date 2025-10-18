package com.openstep.balllinkbe.features.team_record.dto.response;

import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerGameProjection;
import lombok.*;

import java.time.LocalDateTime;

public final class PlayerGameRecordResponse {
    private PlayerGameRecordResponse() {} // 바깥 클래스는 인스턴스화 방지

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Item {
        private Long gameId;
        private LocalDateTime date;
        private String opponent;
        private int pts, reb, ast, stl, blk;
        private int fg2Made, fg2Att, fg3Made, fg3Att, ftMade, ftAtt;

        public static Item from(PlayerGameProjection p) {
            return Item.builder()
                    .gameId(p.getGameId())
                    .date(p.getDate())
                    .opponent(p.getOpponent())
                    .pts(nz(p.getPts())).reb(nz(p.getReb())).ast(nz(p.getAst()))
                    .stl(nz(p.getStl())).blk(nz(p.getBlk()))
                    .fg2Made(nz(p.getFg2Made())).fg2Att(nz(p.getFg2Att()))
                    .fg3Made(nz(p.getFg3Made())).fg3Att(nz(p.getFg3Att()))
                    .ftMade(nz(p.getFtMade())).ftAtt(nz(p.getFtAtt()))
                    .build();
        }

        private static int nz(Integer v) { return v == null ? 0 : v; }
    }
}
