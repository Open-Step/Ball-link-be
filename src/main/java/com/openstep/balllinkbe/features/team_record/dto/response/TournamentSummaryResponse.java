package com.openstep.balllinkbe.features.team_record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "팀의 대회별 누적 기록 리스트 응답")
public class TournamentSummaryResponse {

    private final List<Item> items;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "대회별 요약 통계 정보")
    public static class Item {
        private Long tournamentId;
        private String tournamentName;
        private int games;
        private int wins;
        private int losses;
        private Stats totals;
        private StatsAvg perGame;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "누적 스탯")
    public static class Stats {
        private int pts;
        private int reb;
        private int ast;
        private int stl;
        private int blk;
        private int fg2;
        private int fg3;
        private int ft;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "경기당 평균 스탯")
    public static class StatsAvg {
        private double pts;
        private double reb;
        private double ast;
        private double stl;
        private double blk;
        private double fg2;
        private double fg3;
        private double ft;
    }
}
