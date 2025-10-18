package com.openstep.balllinkbe.features.team_record.repository.projection;

import java.time.LocalDate;

public interface TournamentAggProjection {
    Long getTournamentId();
    String getTournamentName();
    String getSeason();
    String getStatus();
    java.sql.Date getStartDate(); // Native → sql.Date 매핑
    java.sql.Date getEndDate();
    Integer getGames();
    Integer getWins();
    Integer getLosses();
    Integer getPts();
}
