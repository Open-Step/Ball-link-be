package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface SeasonAggProjection {
    String getSeason();
    Integer getGames();
    Integer getPts();
    Integer getReb();
    Integer getAst();
    Integer getStl();
    Integer getBlk();
    Integer getFg2();
    Integer getFg3();
    Integer getFt();
}
