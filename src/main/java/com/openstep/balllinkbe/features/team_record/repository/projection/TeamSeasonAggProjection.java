package com.openstep.balllinkbe.features.team_record.repository.projection;

public interface TeamSeasonAggProjection {
    String getSeason();
    int getGames();
    int getPts();
    int getReb();
    int getAst();
    int getStl();
    int getBlk();
    int getFg2();
    int getFg3();
    int getFt();
}
