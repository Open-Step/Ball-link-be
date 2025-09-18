package com.openstep.balllinkbe.features.team_manage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTeamRequest {
    @NotBlank(message = "팀 이름은 필수입니다.")
    private String name;

    private String shortName;

    @NotNull(message = "창립일은 필수입니다.")
    private LocalDate foundedAt;

    private String region;

    private String description;

    // CDN URL
    private String emblemUrl;

    private Boolean isPublic = true;
}
