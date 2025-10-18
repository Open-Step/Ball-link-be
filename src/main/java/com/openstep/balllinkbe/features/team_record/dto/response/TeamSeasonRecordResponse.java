package com.openstep.balllinkbe.features.team_record.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class TeamSeasonRecordResponse {
    private Long teamId;
    private String season;
    private int games;
    private int wins;
    private int losses;
    private Stats totals;
    private StatsAvg perGame;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Stats { private int pts, reb, ast, stl, blk; }
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class StatsAvg { private double pts, reb, ast, stl, blk; }
}
