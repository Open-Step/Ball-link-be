package com.openstep.balllinkbe.features.tournament.dto.response;

import com.openstep.balllinkbe.domain.tournament.TournamentEntry;
import lombok.Getter;

@Getter
public class EntryResponse {
    private final Long playerId;
    private final String playerName;
    private final String teamName;
    private final Short number;
    private final String position;

    public EntryResponse(TournamentEntry e) {
        this.playerId = e.getPlayer().getId();
        this.playerName = e.getPlayer().getName();
        this.teamName = e.getTeam().getName();
        this.number = e.getNumber();
        this.position = e.getPosition() != null ? e.getPosition().name() : null;
    }
}
