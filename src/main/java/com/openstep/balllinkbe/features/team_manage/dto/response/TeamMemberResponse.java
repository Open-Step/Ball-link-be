package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.TeamMember;
import lombok.Getter;

@Getter
public class TeamMemberResponse {
    private final Long userId;
    private final String name;
    private final String role;

    public TeamMemberResponse(TeamMember member) {
        this.userId = member.getUser().getId();
        this.name = member.getUser().getName();
        this.role = member.getRole().name();
    }
}
