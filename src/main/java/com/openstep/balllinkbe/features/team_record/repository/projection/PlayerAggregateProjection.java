package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface PlayerAggregateProjection {
    Long getPlayerId();
    String getPlayerName();
    Integer getBackNumber();
    Integer getGames();
    Integer getPts();
    Integer getReb();
    Integer getAst();
    Integer getStl();
    Integer getBlk();
    Integer getFg2Made();
    Integer getFg2Att();
    Integer getFg3Made();
    Integer getFg3Att();
    Integer getFtMade();
    Integer getFtAtt();
}
