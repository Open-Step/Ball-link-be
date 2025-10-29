package com.openstep.balllinkbe.features.scrimmage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "자체전 상세 조회 응답 DTO")
public class ScrimmageDetailResponse {

    @Schema(description = "경기 ID", example = "120")
    private Long gameId;

    @Schema(description = "홈팀 이름", example = "서울볼러스")
    private String homeTeamName;

    @Schema(description = "원정팀 이름", example = "인천드리블즈")
    private String awayTeamName;

    @Schema(description = "경기 장소", example = "강남체육관")
    private String venueName;

    @Schema(description = "경기 상태", example = "ONGOING")
    private String state;

    @Schema(description = "시작 시각")
    private LocalDateTime startedAt;

    @Schema(description = "홈팀 라인업")
    private List<PlayerLineup> homeLineup;

    @Schema(description = "원정팀 라인업")
    private List<PlayerLineup> awayLineup;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "라인업 단일 선수 정보")
    public static class PlayerLineup {
        private Long playerId;
        private String name;
        private Integer number;
        private String position;
        private boolean starter;
        private boolean guest;
        private String teamSide;
    }
}
