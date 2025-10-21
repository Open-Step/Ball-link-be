package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.Team;
import lombok.Getter;

@Getter
public class TeamSummaryResponse {
    private final Long id;
    private final String name;
    private final String teamTag;
    private final String displayName;
    private final String region;
    private final String emblemUrl;
    private final boolean isOwner;

    public TeamSummaryResponse(Team team, String cdnUrl, boolean isOwner) {
        this.id = team.getId();
        this.name = team.getName();
        this.teamTag = team.getTeamTag();
        this.displayName = team.getName() + "#" + team.getTeamTag();
        this.region = team.getRegion();
        this.emblemUrl = cdnUrl;
        this.isOwner = isOwner;
    }
}
