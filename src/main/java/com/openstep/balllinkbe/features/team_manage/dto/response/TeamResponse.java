package com.openstep.balllinkbe.features.team_manage.dto.response;

import com.openstep.balllinkbe.domain.team.Team;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class TeamResponse {
    private Long id;
    private String name;
    private String shortName;
    private LocalDate foundedAt;
    private String region;
    private String description;
    private String emblemUrl; // CDN 붙은 URL
    private Boolean isPublic;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeamResponse(Team team, String cdnUrl) {
        this.id = team.getId();
        this.name = team.getName();
        this.shortName = team.getShortName();
        this.foundedAt = team.getFoundedAt();
        this.region = team.getRegion();
        this.description = team.getDescription();
        this.emblemUrl = cdnUrl; // CDN URL 적용
        this.isPublic = team.getIsPublic();
        this.ownerName = team.getOwnerUser() != null ? team.getOwnerUser().getName() : null;
        this.createdAt = team.getCreatedAt();
        this.updatedAt = team.getUpdatedAt();
    }
}
