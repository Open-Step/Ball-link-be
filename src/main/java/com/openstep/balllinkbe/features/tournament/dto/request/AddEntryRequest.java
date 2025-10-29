package com.openstep.balllinkbe.features.tournament.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "경기 엔트리 등록 요청 DTO")
public class AddEntryRequest {

    @Schema(description = "팀 구분 (HOME / AWAY)", example = "HOME")
    private String teamSide;

    @Schema(description = "선수 목록")
    private List<EntryPlayerDto> players;

    @Getter
    @NoArgsConstructor
    @Schema(description = "엔트리 선수 정보 DTO")
    public static class EntryPlayerDto {
        private Long playerId;
        private Integer number;
        private String name;
        private String position;
        private boolean starter;
    }
}
