package com.openstep.balllinkbe.features.game.dto.response;

import com.openstep.balllinkbe.domain.game.Game;
import lombok.Getter;

@Getter
public class GameSummaryResponse {

    private final Long id;
    private final String homeTeamName;
    private final String awayTeamName;
    private final String homeTeamEmblemUrl;
    private final String awayTeamEmblemUrl;
    private final Integer homeScore;
    private final Integer awayScore;
    private final String scheduledDate;
    private final String scheduledTime;
    private final String venueName;

    public GameSummaryResponse(Game g, Integer homeScore, Integer awayScore) {
        this.id = g.getId();
        this.homeTeamName = g.getHomeTeam() != null ? g.getHomeTeam().getName() : "미정";
        this.awayTeamName = g.getAwayTeam() != null ? g.getAwayTeam().getName() : "미정";
        this.homeTeamEmblemUrl = g.getHomeTeam() != null ? g.getHomeTeam().getEmblemUrl() : null;
        this.awayTeamEmblemUrl = g.getAwayTeam() != null ? g.getAwayTeam().getEmblemUrl() : null;
        this.homeScore = homeScore != null ? homeScore : 0;
        this.awayScore = awayScore != null ? awayScore : 0;
        this.scheduledDate = g.getScheduledAt() != null ? g.getScheduledAt().toLocalDate().toString() : null;
        this.scheduledTime = g.getScheduledAt() != null ? g.getScheduledAt().toLocalTime().toString() : null;
        this.venueName = g.getVenue() != null ? g.getVenue().getName() : null;
    }
}
