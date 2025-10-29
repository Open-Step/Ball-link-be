package com.openstep.balllinkbe.features.scrimmage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자체전 생성 요청 DTO")
public class CreateScrimmageRequest {

    @Schema(description = "홈 팀 ID", example = "1")
    private Long homeTeamId;

    @Schema(description = "원정 팀 ID", example = "2")
    private Long awayTeamId;

    @Schema(description = "경기 장소 (optional)", example = "강남체육관")
    private String venueName;

    // 스크림용 단축 생성자 (venueName 없이)
    public CreateScrimmageRequest(Long homeTeamId, Long awayTeamId) {
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.venueName = null;
    }
}
