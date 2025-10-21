package com.openstep.balllinkbe.features.game.dto.response;

import com.openstep.balllinkbe.domain.game.Game;
import lombok.Getter;

@Getter
public class GameSummaryResponse {
    private final Long id;
    private final String homeTeamName;
    private final String awayTeamName;
    private final String scheduledDate;
    private final String scheduledTime;
    private final String venueName;
    private final String venueAddress;

    public GameSummaryResponse(Game g) {
        this.id = g.getId();
        this.homeTeamName = g.getHomeTeam() != null ? g.getHomeTeam().getName() : "미정";
        this.awayTeamName = g.getAwayTeam() != null ? g.getAwayTeam().getName() : "미정";
        this.scheduledDate = g.getScheduledAt() != null ? g.getScheduledAt().toLocalDate().toString() : null;
        this.scheduledTime = g.getScheduledAt() != null ? g.getScheduledAt().toLocalTime().toString() : null;
        this.venueName = g.getVenue() != null ? g.getVenue().getName() : null;
        this.venueAddress = g.getVenue() != null ? g.getVenue().getAddress() : null;
    }
}
