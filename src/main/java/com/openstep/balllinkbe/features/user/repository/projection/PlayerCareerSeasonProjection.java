package com.openstep.balllinkbe.features.user.repository.projection;

public interface PlayerCareerSeasonProjection {
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
