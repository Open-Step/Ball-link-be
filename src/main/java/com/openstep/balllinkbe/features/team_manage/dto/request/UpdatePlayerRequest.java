package com.openstep.balllinkbe.features.team_manage.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdatePlayerRequest {
    private Short number;   // Short로 변경
    private String position;
    private String note;
}
