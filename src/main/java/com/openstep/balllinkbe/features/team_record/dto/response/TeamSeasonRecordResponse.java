package com.openstep.balllinkbe.features.team_record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "팀 시즌별 통산기록 응답")
public class TeamSeasonRecordResponse {

    @Schema(description = "시즌별 기록 리스트")
    private List<Item> items;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "시즌별 기록 항목")
    public static class Item {
        @Schema(description = "시즌 (예: 2024)")
        private String season;

        @Schema(description = "경기 수")
        private int games;

        @Schema(description = "누적 기록")
        private Stats totals;

        @Schema(description = "경기당 평균 기록")
        private StatsAvg perGame;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "팀 누적 기록")
    public static class Stats {
        @Schema(description = "총 득점")
        private int pts;

        @Schema(description = "리바운드")
        private int reb;

        @Schema(description = "어시스트")
        private int ast;

        @Schema(description = "스틸")
        private int stl;

        @Schema(description = "블록")
        private int blk;

        @Schema(description = "2점슛 성공 수")
        private int fg2;

        @Schema(description = "3점슛 성공 수")
        private int fg3;

        @Schema(description = "자유투 성공 수")
        private int ft;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "팀 경기당 평균 기록")
    public static class StatsAvg {
        @Schema(description = "평균 득점")
        private double pts;

        @Schema(description = "평균 리바운드")
        private double reb;

        @Schema(description = "평균 어시스트")
        private double ast;

        @Schema(description = "평균 스틸")
        private double stl;

        @Schema(description = "평균 블록")
        private double blk;

        @Schema(description = "평균 2점슛 성공 수")
        private double fg2;

        @Schema(description = "평균 3점슛 성공 수")
        private double fg3;

        @Schema(description = "평균 자유투 성공 수")
        private double ft;
    }
}
