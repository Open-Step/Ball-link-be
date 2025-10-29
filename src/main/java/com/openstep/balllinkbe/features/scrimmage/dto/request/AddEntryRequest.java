package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "자체전 라인업 등록/수정 요청 DTO")
public class AddEntryRequest {

    @Schema(description = "홈팀 선수 목록")
    private List<EntryPlayerDto> homePlayers;

    @Schema(description = "원정팀 선수 목록")
    private List<EntryPlayerDto> awayPlayers;

    public AddEntryRequest(List<EntryPlayerDto> homePlayers, List<EntryPlayerDto> awayPlayers) {
        this.homePlayers = homePlayers;
        this.awayPlayers = awayPlayers;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "라인업 개별 선수 정보 DTO")
    public static class EntryPlayerDto {
        @Schema(description = "선수 ID (게스트는 null)", example = "15")
        private Long playerId;

        @Schema(description = "등번호", example = "7")
        private Integer number;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "포지션", example = "PG")
        private String position;

        @Schema(description = "스타터 여부", example = "true")
        private boolean starter;
    }
}
