package com.openstep.balllinkbe.features.team_record.repository.projection;

import java.time.LocalDateTime;

public interface PlayerGameProjection {
    Long getGameId();
    LocalDateTime getDate();
    String getOpponent();
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
