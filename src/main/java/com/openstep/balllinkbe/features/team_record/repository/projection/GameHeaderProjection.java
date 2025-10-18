package com.openstep.balllinkbe.features.team_record.repository.projection;

import java.sql.Timestamp;

public interface GameHeaderProjection {
    Long getGameId();
    Long getTournamentId();
    String getTournamentName();
    Timestamp getDate();
    String getVenueName();
    Long getHomeTeamId();
    String getHomeTeamName();
    Long getAwayTeamId();
    String getAwayTeamName();
}
