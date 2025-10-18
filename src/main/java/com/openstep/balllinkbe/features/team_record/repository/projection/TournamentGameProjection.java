package com.openstep.balllinkbe.features.team_record.repository.projection;

import java.sql.Timestamp;

public interface TournamentGameProjection {
    Long getGameId();
    Timestamp getDate();
    String getVenueName();
    String getOpponentName();
    String getState();
    Integer getMyScore();
    Integer getOppScore();
    String getResult();
}
