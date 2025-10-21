package com.openstep.balllinkbe.features.user.dto.response;

import com.openstep.balllinkbe.features.user.repository.projection.PlayerCareerRecentProjection;
import com.openstep.balllinkbe.features.user.repository.projection.PlayerCareerSeasonProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "마이커리어 응답 (최근 경기 + 연도별 통산)")
public class MyCareerResponse {

    @Schema(description = "최근 경기 기록 목록")
    private final List<RecentGame> recentGames;

    @Schema(description = "연도별 통산 기록 목록")
    private final List<SeasonStat> seasonStats;

    // ========================================================
    // 최근 경기 DTO
    // ========================================================
    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "최근 경기 요약")
    public static class RecentGame {
        private Long gameId;
        private String opponent;
        private LocalDateTime date;
        private int pts;
        private int reb;
        private int ast;
        private int stl;
        private int blk;

        // Projection 매핑용 팩토리 메서드
        public static RecentGame from(PlayerCareerRecentProjection p) {
            return new RecentGame(
                    p.getGameId(),
                    p.getOpponent(),
                    p.getDate(),
                    p.getPts(),
                    p.getReb(),
                    p.getAst(),
                    p.getStl(),
                    p.getBlk()
            );
        }
    }

    // ========================================================
    // 시즌별 통산 기록 DTO
    // ========================================================
    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "연도별 통산 기록")
    public static class SeasonStat {
        private String season;
        private int games;
        private int pts;
        private int reb;
        private int ast;
        private int stl;
        private int blk;
        private int fg2;
        private int fg3;
        private int ft;

        // Projection 매핑용 팩토리 메서드
        public static SeasonStat from(PlayerCareerSeasonProjection p) {
            return new SeasonStat(
                    p.getSeason(),
                    p.getGames(),
                    p.getPts(),
                    p.getReb(),
                    p.getAst(),
                    p.getStl(),
                    p.getBlk(),
                    p.getFg2(),
                    p.getFg3(),
                    p.getFt()
            );
        }
    }
}
