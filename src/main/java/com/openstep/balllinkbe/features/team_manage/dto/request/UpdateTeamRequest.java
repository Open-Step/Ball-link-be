package com.openstep.balllinkbe.features.team_manage.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTeamRequest {
    private String description;
    private LocalDate foundedAt;
    private Boolean isPublic;
}
