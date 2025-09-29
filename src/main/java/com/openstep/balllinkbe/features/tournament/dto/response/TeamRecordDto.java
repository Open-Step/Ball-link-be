package com.openstep.balllinkbe.features.tournament.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TeamRecordDto {
    private Long tournamentId; //토너먼트 아이디
    private String tournamentName; //토너먼트 이름

    private int gameCount; //대회 경기수
    private Integer wins = null;  //승
    private Integer losses = null; //패

    private int pts; //총 등점
    private int ast; //어시
    private int reb; //라비운드
    private int stl; //스틸
    private int blk; //블록
    private int fg2; //2점
    private int fg3; //3점
    private int ft; //자유투

    public TeamRecordDto(
            Long tournamentId, String tournamentName,
            Long gameCount, //Long wins, Long losses,
            Long pts, Long ast, Long reb, Long stl, Long blk, Long fg2, Long fg3, Long ft
    ){
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;

        this.gameCount = gameCount == null ? 0 : gameCount.intValue();
        //this.wins = wins == null ? 0 : wins.intValue();
        //this.losses = losses == null ? 0 : losses.intValue();

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
