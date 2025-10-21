package com.openstep.balllinkbe.features.user.repository.projection;

import java.time.LocalDateTime;

public interface PlayerCareerRecentProjection {
    Long getGameId();
    LocalDateTime getDate();
    String getOpponent();
    int getPts();
    int getReb();
    int getAst();
    int getStl();
    int getBlk();
}
