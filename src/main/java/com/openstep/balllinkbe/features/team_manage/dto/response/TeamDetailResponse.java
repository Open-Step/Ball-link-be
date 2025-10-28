package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TeamDetailResponse {
    private final Long id;
    private final String name;
    private final String teamTag;
    private final String displayName;
    private final String shortName;
    private final String description;
    private final LocalDate foundedAt;
    private final Boolean isPublic;
    private final String emblemUrl;
    private final String ownerName;
    private final String ownerProfileUrl;
    private final boolean isOwner;
    private final boolean isIncluded;
    private final long playerCount;
    private final TeamMember.Role myRole;

    public TeamDetailResponse(Team team,
                              long playerCount,
                              String emblemUrl,
                              boolean isOwner,
                              boolean isIncluded,
                              String ownerProfileUrl,
                              TeamMember.Role myRole) {
        this.id = team.getId();
        this.name = team.getName();
        this.teamTag = team.getTeamTag();
        this.displayName = team.getName() + "#" + team.getTeamTag();
        this.shortName = team.getShortName();
        this.description = team.getDescription();
        this.foundedAt = team.getFoundedAt();
        this.isPublic = team.getIsPublic();
        this.emblemUrl = emblemUrl;
        this.ownerName = team.getOwnerUser() != null ? team.getOwnerUser().getName() : null;
        this.ownerProfileUrl = ownerProfileUrl;
        this.isOwner = isOwner;
        this.isIncluded = isIncluded;
        this.playerCount = playerCount;
        this.myRole = myRole;
    }
}
