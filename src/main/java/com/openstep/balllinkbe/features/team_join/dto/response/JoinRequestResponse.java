package com.openstep.balllinkbe.features.team_join.dto.response;

import com.openstep.balllinkbe.domain.team.JoinRequest;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JoinRequestResponse {
    private Long id;
    private String applicantName;
    private String applicantEmail;
    private String position;
    private String status;
    private String bio;
    private String location;
    private LocalDateTime appliedAt;

    public JoinRequestResponse(JoinRequest entity) {
        this.id = entity.getId();
        this.applicantName = entity.getApplicant().getName();
        this.applicantEmail = entity.getApplicant().getEmail();
        this.position = entity.getPosition() != null ? entity.getPosition().name() : null;
        this.status = entity.getStatus().name();
        this.bio = entity.getBio();
        this.location = entity.getLocation();
        this.appliedAt = entity.getAppliedAt();
    }
}
