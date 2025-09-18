package com.openstep.balllinkbe.features.team_manage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamSummaryResponse {
    private Long id;
    private String name;
    private String shortName;
}
