package com.openstep.balllinkbe.features.tournament.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddTournamentTeamRequest {
    private Long teamId;
    private Integer seed; // 시드 순서
}
