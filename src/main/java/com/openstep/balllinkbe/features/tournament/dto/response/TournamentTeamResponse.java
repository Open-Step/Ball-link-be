package com.openstep.balllinkbe.features.tournament.dto.response;

import com.openstep.balllinkbe.domain.tournament.TournamentTeam;
import lombok.Getter;

@Getter
public class TournamentTeamResponse {
    private final Long id;
    private final Long teamId;
    private final String teamName;
    private final Integer seed;

    public TournamentTeamResponse(TournamentTeam tt) {
        this.id = tt.getId();
        this.teamId = tt.getTeam().getId();
        this.teamName = tt.getTeam().getName();
        this.seed = tt.getSeed();
    }
}
