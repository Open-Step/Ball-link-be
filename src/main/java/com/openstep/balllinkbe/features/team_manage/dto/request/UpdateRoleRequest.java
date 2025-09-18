package com.openstep.balllinkbe.features.team_manage.dto.request;

import com.openstep.balllinkbe.domain.team.TeamMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateRoleRequest {
    @Schema(description = "변경할 역할 (MANAGER | PLAYER)", example = "MANAGER")
    private String role;

    public TeamMember.Role toEnum() {
        return TeamMember.Role.valueOf(role.toUpperCase());
    }
}