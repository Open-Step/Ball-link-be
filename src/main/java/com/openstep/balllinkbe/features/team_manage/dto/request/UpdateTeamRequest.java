package com.openstep.balllinkbe.features.team_manage.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeamRequest {
    private String description;
    private String emblemFileId;
    private Integer foundedYear;
    private String colorPrimary;
}
