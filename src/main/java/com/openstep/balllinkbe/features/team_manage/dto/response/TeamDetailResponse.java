package com.openstep.balllinkbe.features.team_manage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TeamDetailResponse {
    private Long id;
    private String name;
    private String shortName;
    private Integer foundedYear;
    private String region;
    private String description;
    private String emblemFileId;
    private String colorPrimary;
    private String ownerName;   // users.name
    private Boolean isPublic;   // 공개 여부
}
