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

    @Schema(description = "홈 팀 이름", example = "1")
    private String homeTeamName;

    @Schema(description = "원정 팀 이름", example = "2")
    private String awayTeamName;

    @Schema(description = "경기 장소 (optional)", example = "강남체육관")
    private String venueName;

    // 스크림용 단축 생성자 (venueName 없이)
    public CreateScrimmageRequest(String homeTeamName, String awayTeamName) {
        this.homeTeamName = homeTeamName;
        this.awayTeamName = awayTeamName;
        this.venueName = null;
    }
}
