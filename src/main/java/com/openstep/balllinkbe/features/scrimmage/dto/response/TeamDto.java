package com.openstep.balllinkbe.features.scrimmage.dto.response;

import com.openstep.balllinkbe.domain.team.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "팀 정보")
public record TeamDto(
        @Schema(description = "팀 ID", example = "1")
        Long id,
        @Schema(description = "팀 이름", example = "오픈스텝")
        String name,
        @Schema(description = "팀 태그", example = "OpenStep")
        String teamTag,
        @Schema(description = "표시 이름", example = "OpenStep")
        String displayName,
        @Schema(description = "지역", example = "서울")
        String region,
        @Schema(description = "팀 엠블럼 URL", example = "https://www.오픈스텝.com/teams/1/1_20250926132000.png")
        String emblemUrl // CDN 붙은 URL
){

    // Team 정적 팩토리 메서드
    public static TeamDto from(Team team){
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .teamTag(team.getTeamTag())
                .displayName(team.getTeamTag())
                .region(team.getRegion())
                .emblemUrl(team.getEmblemUrl())
                .build();
    }
}