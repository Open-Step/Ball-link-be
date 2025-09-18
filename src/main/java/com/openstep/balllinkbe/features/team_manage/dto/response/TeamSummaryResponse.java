package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.Team;
import lombok.Getter;

@Getter
public class TeamSummaryResponse {
    private final Long id;
    private final String name;
    private final String region;
    private final String emblemUrl;

    public TeamSummaryResponse(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.region = team.getRegion();
        this.emblemUrl = team.getEmblemUrl();
    }
}
