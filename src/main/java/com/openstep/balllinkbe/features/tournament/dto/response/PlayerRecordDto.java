package com.openstep.balllinkbe.features.tournament.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerRecordDto {
    @Setter
    private int rank;
    private Long playerId;
    private String playerName;
    private int backNumber;
    private int gameCount;
    private int pts;
    private int ast;
    private int reb;
    private int stl;
    private int blk;
    private int fg2;
    private int fg3;
    private int ft;

    public PlayerRecordDto(
            Integer rank, Long playerId, String playerName, Short backNumber,
            Long gameCount, Long pts, Long ast,Long reb, Long stl, Long blk, Long fg2, Long fg3, Long ft
    ) {
        this.rank = rank == null ? 0 : rank;
        this.playerId = playerId;
        this.playerName = playerName;
        this.backNumber = backNumber == null ? 0 : backNumber;
        this.gameCount = gameCount == null ? 0 : gameCount.intValue();
        this.pts = pts == null ? 0 : pts.intValue();
        this.ast = ast == null ? 0 : ast.intValue();
        this.reb = reb == null ? 0 : reb.intValue();
        this.stl = stl == null ? 0 : stl.intValue();
        this.blk = blk == null ? 0 : blk.intValue();
        this.fg2 = fg2 == null ? 0 : fg2.intValue();
        this.fg3 = fg3 == null ? 0 : fg3.intValue();
        this.ft = ft == null ? 0 : ft.intValue();
    }

}
