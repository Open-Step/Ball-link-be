package com.openstep.balllinkbe.features.tournament.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 대회 참가팀 등록 및 엔트리(라인업) 요청 DTO
 */
@Getter
@Setter
public class AddTournamentTeamRequest {

    @Schema(description = "팀 ID", example = "3")
    private Long teamId;  // 추가됨

    @Schema(description = "시드 번호 (선택)", example = "1")
    private Integer seed; // 추가됨

    @Schema(description = "팀 구분 (HOME / AWAY)", example = "HOME")
    private String teamSide;

    @Schema(description = "팀의 엔트리 선수 목록")
    private List<EntryPlayerDto> players = new ArrayList<>();

    @Getter
    @Setter
    public static class EntryPlayerDto {

        @Schema(description = "회원 선수 ID (게스트일 경우 null)", example = "12")
        private Long playerId;

        @Schema(description = "게스트 이름 (회원선수면 null)", example = "홍길동")
        private String name;

        @Schema(description = "등번호", example = "7")
        private Integer number;

        @Schema(description = "포지션 (PG, SG, SF, PF, C)", example = "PG")
        private String position;

        @Schema(description = "선발 여부", example = "true")
        private boolean starter;
    }
}
